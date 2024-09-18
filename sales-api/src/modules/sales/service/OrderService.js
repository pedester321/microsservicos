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
      this.validateOrderData(orderData);
      const { authUser } = req;
      const { authorization } = req.headers;
      let order = this.createInitialOrderData(orderData, authUser);
      await this.validateProductStock(order, authorization);
      let createdOrder = await OrderRepository.save(order);
      this.sendMessage(createdOrder);
      return {
        status: httpStatus.SUCCESS,
        createdOrder,
      };
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
        console.warn("The order message was incomplete.");
      }
    } catch (error) {
      console.error("Could not parse order message from queue.");
      console.error(error.message);
    }
  }

  sendMessage(createdOrder) {
    const message = {
      salesId: createdOrder.id,
      products: createdOrder.products,
    };
    sendMessageToProductStockUpdateQueue(message);
  }

  async findById(req) {
    try {
      const { id } = req.params;
      this.validateInformedId(id);
      const existingOrder = await OrderRepository.findById(id);
      if (!existingOrder) {
        throw new OrderException(
          httpStatus.BAD_REQUEST,
          "No order was found with that ID."
        );
      }
      return {
        status: httpStatus.SUCCESS,
        existingOrder,
      };
    } catch (error) {
      return {
        status: error.status ? error.status : httpStatus.INTERNAL_SERVER_ERROR,
        message: error.message,
      };
    }
  }

  async findAll() {
    try {
      const allOrders = await OrderRepository.findAll();
      if (!allOrders) {
        throw new OrderException(
          httpStatus.BAD_REQUEST,
          "No orders found."
        );
      }
      return {
        status: httpStatus.SUCCESS,
        allOrders,
      };
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
      const orders = await OrderRepository.findByProductId(productId);
      if (!orders) {
        throw new OrderException(
          httpStatus.BAD_REQUEST,
          "No orders found for the given productId."
        );
      }
      return {
        status: httpStatus.SUCCESS,
        salesIds: orders.map((order) =>{
          return order.id;
        }),
      };
    } catch (error) {
      return {
        status: error.status ? error.status : httpStatus.INTERNAL_SERVER_ERROR,
        message: error.message,
      };
    }
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
  async validateProductStock(order, token) {
    let isStockOk = await ProductClient.checkProductStock(order, token);
    if (!isStockOk) {
      throw new OrderException(
        httpStatus.BAD_REQUEST,
        "A product is out of stock."
      );
    }
  }
}

export default new OrderService();
