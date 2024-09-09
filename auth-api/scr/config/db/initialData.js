import bcrypt from "bcrypt";
import User from "../../modules/user/model/User.js";

export async function createInitalData() {
    await User.sync({force: true});

    let password = await bcrypt.hash('123321', 10)

    let firstUser = await User.create({
        name: "User de Testes",
        email: 'testuser@gmail.com',
        password:  password,
    })
}