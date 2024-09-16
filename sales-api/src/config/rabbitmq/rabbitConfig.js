import amqp from "amqplib/callback_api.js";

import {
  PRODUCT_STOCK_UPDATE_QUEUE,
  PRODUCT_STOCK_UPDATE_ROUTING_KEY,
  PRODUCT_TOPIC,
  SALES_CONFIRMATION_QUEUE,
  SALES_CONFIRMATION_ROUTING_KEY,
} from "./queue.js";
import { RABBIT_MQ_URL, NODE_ENV } from "../constants/secrets.js";
import { listenToSalesConfirmationQueue } from "../../modules/sales/rabbitmq/salesConfirmationListener.js";

const TWO_SECONDS = 2000;
const HALF_A_MINUTE = 30000;
const CONTAINER_ENV = "container";

export async function connectRabbitMq() {
  if (CONTAINER_ENV === NODE_ENV) {
    console.info("Waiting for rabbitMQ to start...");
    setTimeout(() => {
      connectRabbitMqAndCreateQueues();
    }, HALF_A_MINUTE);
  } else {
    connectRabbitMqAndCreateQueues();
  }
}

async function connectRabbitMqAndCreateQueues() {
  amqp.connect(RABBIT_MQ_URL, (error, connection) => {
    if (error) {
      throw error;
    }

    createQueue(
      connection,
      PRODUCT_STOCK_UPDATE_QUEUE,
      PRODUCT_STOCK_UPDATE_ROUTING_KEY,
      PRODUCT_TOPIC
    );
    createQueue(
      connection,
      SALES_CONFIRMATION_QUEUE,
      SALES_CONFIRMATION_ROUTING_KEY,
      PRODUCT_TOPIC
    );
    setTimeout(function () {
      connection.close();
    }, TWO_SECONDS);
  });
  setTimeout(function () {
    listenToSalesConfirmationQueue();
  }, TWO_SECONDS);
}

function createQueue(connection, queue, routingKey, topic) {
  connection.createChannel((error, channel) => {
    if (error) {
      throw error;
    }
    channel.assertExchange(topic, "topic", {
      durable: true,
    });
    channel.assertQueue(queue, {
      durable: true,
    });
    channel.bindQueue(queue, topic, routingKey);
  });
}
