import express from "express";

import userRoutes from "./scr/modules/user/routes/UserRoutes.js";
import tracing from "./scr/config/tracing.js";
import { createInitalData } from "./scr/config/db/initialData.js";

const app = express();
const env = process.env;
const PORT = env.PORT || 8080;
const CONTAINER_ENV = "container";

app.get("/", (req, res) => {
  return res.status(200).json(getOkResponse());
});
app.get("/api/status", (req, res) => {
  return res.status(200).json(getOkResponse());
});
app.use(express.json());
app.use(tracing);
app.use(userRoutes);

function getOkResponse() {
  return {
    servise: "Sales-API",
    status: "up",
    httpStatus: 200,
  };
}

// generateInitialData();

// function generateInitialData() {
//   if (env.NODE_ENV !== CONTAINER_ENV) {
//     createInitalData();
//   }
// }

app.listen(PORT, () => {
  console.log(`Server started successfully at port ${PORT}`);
});
