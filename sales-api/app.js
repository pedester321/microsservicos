import express from "express";

import { connectMongoDb } from "./src/config/db/mongoDbConfig.js";
import { createInitialData } from "./src/config/db/InitialData.js";
import checkToken from "./src/config/auth/checkToken.js";
import { connectRabbitMq } from "./src/config/rabbitmq/rabbitConfig.js";
import orderRoutes from "./src/modules/sales/routes/OrderRoutes.js";
import tracing from "./src/config/tracing.js";

const app = express();
const env = process.env;
const PORT = env.PORT || 8082;

connectMongoDb();
connectRabbitMq();
//createInitialData();

app.get("/", async (req, res) => {
  return res.status(200).json(getOkResponse());
});

app.use(express.json());
app.use(tracing);
app.use(checkToken);
app.use(orderRoutes);

app.get("/api/status", async (req, res) => {
  return res.status(200).json(getOkResponse());
});

function getOkResponse() {
  return {
    servise: "Auth-API",
    status: "up",
    httpStatus: 200,
  };
}

app.listen(PORT, () => {
  console.log(`Server started successfully at port ${PORT}`);
});
