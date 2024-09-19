import OrderRepository from "../repository/OrderRepository.js";
import { sendMessageToProductStockUpdateQueue } from "../../product/rabbitmq/productStockUpdateSender.js";
import * as httpStatus from "../../../config/constants/httpStatus.js";
import { PENDING, ACCEPTED, REJECTED } from "../status/OrderStatus.js";
import OrderException from "../exception/OrderException.js";
import ProductClient from "../../product/client/ProductClient.js";

class OrderService {
  async createOrder(req) {
    try {
      let orderData = req.body;
      const { transactionid, serviceid } = req.headers;
      this.tracingLog(
        "Request to POST new order with data",
        orderData,
        transactionid,
        serviceid
      );
      this.validateOrderData(orderData);
      const { authUser } = req;
      const { authorization } = req.headers;
      let order = this.createInitialOrderData(orderData, authUser);
      await this.validateProductStock(order, authorization, transactionid);
      let createdOrder = await OrderRepository.save(order);
      this.sendMessage(createdOrder, transactionid);
      let response = {
        status: httpStatus.SUCCESS,
        createdOrder,
      };
      this.tracingLog(
        "Response to POST new order with data",
        response,
        transactionid,
        serviceid
      );
      return response;
    } catch (error) {
      return {
        status: error.status ? error.status : httpStatus.INTERNAL_SERVER_ERROR,
        message: error.message,
      };
    }
  }

  createInitialOrderData(orderData, authUser) {
    return {
      status: PENDING,
      user: authUser,
      updatedAt: new Date(),
      createdAt: new Date(),
      products: orderData.products,
    };
  }

  async updateOrder(orderMessage) {
    try {
      const order = JSON.parse(orderMessage);
      if (order.salesId && order.status) {
        let existingOrder = await OrderRepository.findById(order.salesId);
        if (existingOrder && order.status !== existingOrder.status) {
          existingOrder.status = order.status;
          existingOrder.updatedAt = new Date();
          await OrderRepository.save(existingOrder);
        }
      } else {
        console.warn(`The order message was incomplete. TransactionID: ${orderMessage.transactionid}`);
      }
    } catch (error) {
      console.error("Could not parse order message from queue.");
      console.error(error.message);
    }
  }

  sendMessage(createdOrder, transactionid) {
    const message = {
      salesId: createdOrder.id,
      products: createdOrder.products,
      transactionid,
    };
    sendMessageToProductStockUpdateQueue(message);
  }

  async findById(req) {
    try {
      const { id } = req.params;
      const { transactionid, serviceid } = req.headers;
      this.tracingLog(
        `Request to GET order by ID ${id}`,
        null,
        transactionid,
        serviceid
      );
      this.validateInformedId(id);
      const existingOrder = await OrderRepository.findById(id);
      if (!existingOrder) {
        throw new OrderException(
          httpStatus.BAD_REQUEST,
          "No order was found with that ID."
        );
      }
      let response = {
        status: httpStatus.SUCCESS,
        existingOrder,
      };
      this.tracingLog(
        `Response to GET order by ID ${id} with data`,
        response,
        transactionid,
        serviceid
      );
      return response;
    } catch (error) {
      return {
        status: error.status ? error.status : httpStatus.INTERNAL_SERVER_ERROR,
        message: error.message,
      };
    }
  }

  async findAll() {
    try {
      const { transactionid, serviceid } = req.headers;
      this.tracingLog(
        `Request to GET all orders`,
        null,
        transactionid,
        serviceid
      );
      const allOrders = await OrderRepository.findAll();
      if (!allOrders) {
        throw new OrderException(httpStatus.BAD_REQUEST, "No orders found.");
      }
      let response = {
        status: httpStatus.SUCCESS,
        allOrders,
      };
      this.tracingLog(
        "Response to GET all orders with data",
        response,
        transactionid,
        serviceid
      );
    } catch (error) {
      return {
        status: error.status ? error.status : httpStatus.INTERNAL_SERVER_ERROR,
        message: error.message,
      };
    }
  }

  async findByProductId(req) {
    try {
      const { productId } = req.params;
      this.validateInformedProductId(productId);
      const { transactionid, serviceid } = req.headers;
      this.tracingLog(
        `Request to GET all orders by productId ${productId}`,
        null,
        transactionid,
        serviceid
      );
      const orders = await OrderRepository.findByProductId(productId);
      if (!orders) {
        throw new OrderException(
          httpStatus.BAD_REQUEST,
          "No orders found for the given productId."
        );
      }
      let response = {
        status: httpStatus.SUCCESS,
        salesIds: orders.map((order) => {
          return order.id;
        }),
      };
      this.tracingLog(
        `Response to GET all orders by productId ${productId}:`,
        response,
        transactionid,
        serviceid
      );
      return response;
    } catch (error) {
      return {
        status: error.status ? error.status : httpStatus.INTERNAL_SERVER_ERROR,
        message: error.message,
      };
    }
  }

  tracingLog(message, data = null, transactionid, serviceid) {
    let initalLog = `${message} ${JSON.stringify(data)}`;
    if (data == null) {
      initalLog = `${message} `;
    }
    console.log(
      `${initalLog} | [transactionID]: ${transactionid} | serviceID: ${serviceid}`
    );
  }

  //VALIDATIONS
  validateInformedId(id) {
    if (!id) {
      throw new OrderException(
        httpStatus.BAD_REQUEST,
        "The order ID must be informed."
      );
    }
  }
  validateInformedProductId(productId) {
    if (!productId) {
      throw new OrderException(
        httpStatus.BAD_REQUEST,
        "The order productId must be informed."
      );
    }
  }
  validateOrderData(data) {
    if (!data || !data.products) {
      throw new OrderException(
        httpStatus.BAD_REQUEST,
        "The products must be informed."
      );
    }
  }
  async validateProductStock(order, token, transactionid) {
    let isStockOk = await ProductClient.checkProductStock(
      order,
      token,
      transactionid
    );
    if (!isStockOk) {
      throw new OrderException(
        httpStatus.BAD_REQUEST,
        "A product is out of stock."
      );
    }
  }
}

export default new OrderService();
