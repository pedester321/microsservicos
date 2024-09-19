import axios from "axios";

import { PRODUCT_API_URL } from "../../../config/constants/secrets.js";

class ProductClient {
  async checkProductStock(productsData, token, transactionid) {
    try {
      const headers = {
        Authorization: token,
        transactionid,
      };
      let response = false;
      await axios
        .post(
          `${PRODUCT_API_URL}/check-stock`,
          { products: productsData.products },
          {
            headers,
          }
        )
        .then((res) => {
          response = true;
          console.info(
            `Sending request to Product API with data: ${JSON.stringify(
              productsData
            )} and transactionID ${transactionid}`
          );
        })
        .catch((error) => {
          console.error(
            `Error response from PRODUCT-API. TransactionID: ${transactionid}`
          );
          response = false;
        });
      return response;
    } catch (error) {
      console.error(
        `Error response from PRODUCT-API. TransactionID: ${transactionid}`
      );
      console.error(error.message);
      return false;
    }
  }
}
export default new ProductClient();
