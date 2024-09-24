import express from "express";

import userRoutes from "./scr/modules/user/routes/UserRoutes.js";
import tracing from "./scr/config/tracing.js";

const app = express();
const env = process.env;
const PORT = env.PORT || 8080;

app.use(express.json());
app.use(tracing);
app.use(userRoutes);

app.get("/api/status", (req, res) => {
  return res.status(200).json({
    servise: "Auth-API",
    status: "up",
    httpStatus: 200,
  });
});

app.listen(PORT, () => {
  console.log(`Server started successfully at port ${PORT}`);
});
