package com.design2net.the_house.network

enum class RequestCode (val code: Int){
    GET_TIENDAS(5),
    GET_ORDENES(10),
    GET_PRODUCTS(15),
    GET_DEFINITIONS(20),
    COMPLETE_ORDER(25),
    ADD_ITEM(30),
    ADD_BAG(35),
    DELETE_BIN(40),
    DELETE_BAG(45),
    GET_BAGS(50),
    ADD_PRODUCTS(55),
    GET_BAG(60),
    UPDATE_BAG(65),
    GET_BINES(70),
    UPDATE_BIN(75),
    CREATE_BIN(80),
    LOGIN(85),
    LOGOUT(90),
    COMPLETE_PRODUCT(95),
    NOT_AVAILABLE(100),
    GET_AISLES(105),
    RESET_PRODUCT(110),
    GET_SUSTITUTO(115),
    ENVIAR_SUSTITUTO(120),
    GET_CHAT(125),
    GET_SUSTITUTOS(130),
    SEND_MESSAGE(135),
    GET_NOTIFICATIONS(140),
    PICK_PRODUCTS(145),
    CHECK_APP(150)
}