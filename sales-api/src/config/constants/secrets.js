const env = process.env;

export const NODE_ENV = env.NODE_ENV ? env.NODE_ENV : "node";

export const MONGO_DB_URL = env.MONGO_DB_URL
  ? env.MONGO_DB_URL
  : "mongodb://localhost:27017/sales-db";

export const API_SECRET = env.API_SECRET
  ? env.API_SECRET
  : "uR1zYg8Z2/3kSbz6rds34SDsdh89jsdfgkjsdfslkdi==";

export const RABBIT_MQ_URL = env.RABBIT_MQ_URL
  ? env.RABBIT_MQ_URL
  : "amqp://localhost:5672";

export const PRODUCT_API_URL = env.PRODUCT_API_URL
  ? env.PRODUCT_API_URL
  : "http://localhost:8081/api/product";
