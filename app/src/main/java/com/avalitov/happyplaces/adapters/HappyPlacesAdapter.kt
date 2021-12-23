package com.avalitov.happyplaces.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.avalitov.happyplaces.R
import com.avalitov.happyplaces.models.HappyPlaceModel

open class HappyPlacesAdapter(
    private val context : Context,
    private val list: ArrayList<HappyPlaceModel>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        return PlaceViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_happy_place,
                parent,
                false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if(holder is PlaceViewHolder) {
            holder.ivImage.setImageURI(Uri.parse(model.imagePath))
            holder.tvTitle.text = model.title
            holder.tvDescription.text = model.description

            holder.itemView.setOnClickListener{
                if(onClickListener != null){
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }


    class PlaceViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var tvTitle: TextView = itemView.findViewById(R.id.tv_item_title)
        var tvDescription: TextView = itemView.findViewById(R.id.tv_item_description)
        var ivImage: ImageView = itemView.findViewById(R.id.iv_item_place_image)
    }

    interface OnClickListener {
        fun onClick(position: Int, model: HappyPlaceModel)
    }
    
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    fun setOnClickListener(onClickListener: HappyPlacesAdapter.OnClickListener, function: () -> Unit) {

    }


}