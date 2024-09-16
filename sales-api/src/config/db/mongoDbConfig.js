import mongoose from "mongoose"

import {MONGO_DB_URL} from '../constants/secrets.js'

export function connectMongoDb(){
    mongoose.connect(MONGO_DB_URL, {
        // useNewUrlParser: true,

    })
    mongoose.connection.on('connected', function(){
        console.log('Successfully connected to MongoDB!')
    })

    mongoose.connection.on('error', function(){
        console.error('Error connecting to MongoDB!')
    })
}