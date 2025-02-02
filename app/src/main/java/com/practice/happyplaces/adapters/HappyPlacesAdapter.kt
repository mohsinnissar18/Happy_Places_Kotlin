package com.practice.workoutapp

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practice.happyplaces.databinding.ItemHappyPlaceBinding
import com.practice.happyplaces.models.HappyPlacesModel


class HappyPlacesAdapter(
    private val context: Context,
    private var list : ArrayList<HappyPlacesModel>
    ) : RecyclerView.Adapter<HappyPlacesAdapter.ViewHolder>(){

    class ViewHolder (binding: ItemHappyPlaceBinding) : RecyclerView.ViewHolder(binding.root){

        val ivPlaceImage = binding.ivPlaceImage
        val tvTitle = binding.tvTitle
        val tvDescription = binding.tvDescription

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHappyPlaceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val model = list[position]

        if (holder is ViewHolder) {
            Log.e("ImageURI", model.image +"Hello")
            holder.ivPlaceImage.setImageURI(Uri.parse(model.image))
            holder.tvTitle.text = model.title
            holder.tvDescription.text = model.description
        }

    }
}