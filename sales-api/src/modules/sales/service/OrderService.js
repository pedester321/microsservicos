import OrderRepository from "../repository/OrderRepository";
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
      let order = this.createInitialOrderData(order, authUser);
      await this.validateProductStock(order, authorization);
      let createdOrder = await OrderRepository.save(order);
      this.sendMessage(createdOrder);
      return {
        status: httpStatus.SUCCESS,
        accessToken,
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
      products: orderData,
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

  validateOrderData(data) {
    if (!data || !data.products) {
      throw new OrderException(
        httpStatus.BAD_REQUEST,
        "The products must be informed."
      );
    }
  }
  async validateProductStock(order, token) {
    let stockIsOut = await ProductClient.checkProductStock(
      order.products,
      token
    );
    if (!stockIsOut) {
      throw new OrderException(
        httpStatus.BAD_REQUEST,
        "The product is out of stock."
      );
    }
  }

  sendMessage(createdOrder) {
    const message = {
      salesId: createdOrder.salesId,
      products: createdOrder.products,
    };
    sendMessageToProductStockUpdateQueue(message);
  }
}

export default new OrderService();
