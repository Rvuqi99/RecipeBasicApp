package my.tutorials.recipeapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class RecipeAdapter(private val context: Context, private val recipeList: ArrayList<RecipesModel>) : RecyclerView.Adapter<RecipeAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recipe_view_holder, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = recipeList[position]
        //var clicked = 0
        //var background = 1

        /*if(deselectList.isNotEmpty()){
            if(deselectList.contains(currentItem.id)){
                holder.button.setBackgroundResource(R.drawable.button_profile_country)
                background = 2
            } else {
                holder.button.setBackgroundResource(R.drawable.button_profile)
                background = 1
            }
        }*/

        val uri : Uri = Uri.parse(currentItem.image)
        //Log.d("uri", uri.toString())
        Picasso.get().load(uri).resize(200,200).centerCrop().into(holder.image)

        holder.tvName.text = "Name: ${currentItem.name}"
        holder.tvType.text = "Type: ${currentItem.type.substring(0, 1).toUpperCase() + currentItem.type.substring(1).toLowerCase()}"
        //holder.tvIngredients.text = "Ingredients: ${currentItem.ingredients}"
        //holder.tvSteps.text = "Steps: ${currentItem.steps}"

        holder.btnInfo.setOnClickListener {
            val i = Intent(context, AddEditRecipe::class.java)
            i.putExtra("Mode", "E")
            i.putExtra("Id", currentItem.id)
            context.startActivity(i)
        }
    }

    override fun getItemCount(): Int {
        return this.recipeList.count()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val tvType: TextView = itemView.findViewById(R.id.tv_type)
        val btnInfo : AppCompatImageView = itemView.findViewById(R.id.btn_info)
        val image : ImageView = itemView.findViewById(R.id.iv_image)
        //val tvIngredients: TextView = itemView.findViewById(R.id.tv_ingredients)
        //val tvSteps: TextView = itemView.findViewById(R.id.tv_steps)
    }
}