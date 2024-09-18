import axios from "axios";

import { PRODUCT_API_URL } from "../../../config/constants/secrets.js";

class ProductClient {
  async checkProductStock(productsData, token) {
    try {
      const headers = {
        Authorization: token,
      };
      let response = false;
      await axios
        .post(`${PRODUCT_API_URL}/check-stock`,{products: productsData.products}, {
          headers,
        })
        .then((res) => {
          response = true;
          console.info(
            `Sending request to Product API with data: ${JSON.stringify(
              productsData
            )}`
          );
        })
        .catch((error) => {
          response = false;
        });
      return response;
    } catch (error) {
      console.error(error.message);
      return false;
    }
  }
}
export default new ProductClient();
