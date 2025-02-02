package com.practice.happyplaces.models

data class HappyPlacesModel(

    val id : Int,
    val title : String,
    val image : String,
    val description : String,
    val date : String,
    val location : String,
    val latitude : Double,
    val longitude : Double
)