import Order from "../../modules/Order.js";

export async function createInitialData(){
    await Order.collection.drop();
    let order1 = await Order.create({
        products: [
            {
                productId: 1001,
                quantity: 2
            },
            {
                productId: 1003,
                quantity: 1
            },
            {
                productId: 1002,
                quantity: 2
            },
        ],
        user: {
            id: '123',
            name: 'User de Teste',
            email: 'usertest@gmail.com'
        },
        status: 'APPROVED',
        createdAt: new Date(),
        updatedAt: new Date()
    })

    let order2 = await Order.create({
        products: [
            {
                productId: 1001,
                quantity: 2
            },
            {
                productId: 1003,
                quantity: 1
            },
        ],
        user: {
            id: '124',
            name: 'User de Teste 2',
            email: 'usertest2@gmail.com'
        },
        status: 'REJECTED',
        createdAt: new Date(),
        updatedAt: new Date()
    })

    let InitialData  = await Order.find()
    console.info(`Initial data was created: ${JSON.stringify(InitialData, undefined, 4)}`)
}