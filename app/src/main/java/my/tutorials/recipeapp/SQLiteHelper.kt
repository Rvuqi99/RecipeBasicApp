package my.tutorials.recipeapp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import kotlin.random.Random

class SQLiteHelper (context : Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "recipe_db"
        private const val TBL_RECIPE = "recipes_tbl"
        private const val ID = "ID"
        private const val TYPE = "TYPE"
        private const val NAME = "NAME"
        private const val INGREDIENTS = "INGREDIENTS"
        private const val STEPS = "STEPS"
        private const val IMAGE = "IMAGE"
    }

    override fun onCreate(db: SQLiteDatabase?){
        val createTblRecipe = ("CREATE TABLE $TBL_RECIPE ($ID INTEGER PRIMARY KEY, $TYPE TEXT, $NAME TEXT, $INGREDIENTS TEXT, $STEPS TEXT, $IMAGE TEXT)")
        db?.execSQL(createTblRecipe)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int){
        db!!.execSQL("DROP TABLE IF EXISTS $TBL_RECIPE")
        onCreate(db)
    }

    fun insertRecipe(recipe: RecipesModel): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(ID, recipe.id)
        contentValues.put(TYPE, recipe.type)
        contentValues.put(NAME, recipe.name)
        contentValues.put(INGREDIENTS, recipe.ingredients)
        contentValues.put(STEPS, recipe.steps)
        contentValues.put(IMAGE, recipe.image)

        //Log.d("content", contentValues.toString())
        val success = db.insert(TBL_RECIPE, null, contentValues)
        db.close()
        return success
    }

    @SuppressLint("Range")
    fun getAllRecipe(): ArrayList<RecipesModel> {
        val recipeList: ArrayList<RecipesModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_RECIPE"
        val db = this.readableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id : String
        var type : String
        var name : String
        var ingredients : String
        var steps : String
        var image : String

        if(cursor.moveToFirst()){
            do{
                id = cursor.getString(cursor.getColumnIndex("ID"))
                type = cursor.getString(cursor.getColumnIndex("TYPE"))
                name = cursor.getString(cursor.getColumnIndex("NAME"))
                ingredients = cursor.getString(cursor.getColumnIndex("INGREDIENTS"))
                steps = cursor.getString(cursor.getColumnIndex("STEPS"))
                image = cursor.getString(cursor.getColumnIndex("IMAGE"))

                val rcp = RecipesModel(id = id, type = type, name = name, ingredients = ingredients, steps = steps, image = image)
                recipeList.add(rcp)
            } while (cursor.moveToNext())
        }

        return recipeList
    }

    @SuppressLint("Range")
    fun getRecipe(id : Int) : RecipesModel {
        val recipes = RecipesModel()
        val selectQuery = "SELECT * FROM $TBL_RECIPE WHERE $ID = $id"
        val db = this.writableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return RecipesModel()
        }

        cursor.moveToFirst()
        recipes.id = cursor.getString(cursor.getColumnIndex("ID"))
        recipes.type = cursor.getString(cursor.getColumnIndex("TYPE"))
        recipes.name= cursor.getString(cursor.getColumnIndex("NAME"))
        recipes.ingredients = cursor.getString(cursor.getColumnIndex("INGREDIENTS"))
        recipes.steps = cursor.getString(cursor.getColumnIndex("STEPS"))
        recipes.image = cursor.getString(cursor.getColumnIndex("IMAGE"))
        cursor.close()

        return recipes
    }

    fun updateRecipe(recipe: RecipesModel): Int {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(ID, recipe.id)
        contentValues.put(TYPE, recipe.type)
        contentValues.put(NAME, recipe.name)
        contentValues.put(INGREDIENTS, recipe.ingredients)
        contentValues.put(STEPS, recipe.steps)
        contentValues.put(IMAGE, recipe.image)

        //Log.d("content", contentValues.toString())
        val success = db.update(TBL_RECIPE, contentValues,  "ID=" + recipe.id, null)
        db.close()
        return success
    }

    fun deleteRecipeById(id: Int): Int {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(ID, id)

        //Log.d("content", contentValues.toString())
        val success = db.delete(TBL_RECIPE, "ID=$id", null)
        db.close()
        return success
    }
}