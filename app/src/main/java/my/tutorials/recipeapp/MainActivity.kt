package my.tutorials.recipeapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class MainActivity : AppCompatActivity(){
    private lateinit var spinner: Spinner
    private lateinit var rvRecipe: RecyclerView
    private lateinit var btnAdd: Button
    private lateinit var btnLogout: Button
    private lateinit var sqLiteHelper: SQLiteHelper
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferences2: SharedPreferences
    private lateinit var firebaseAuth : FirebaseAuth

    var typeList : ArrayList<String> = ArrayList()
    var recipeTypeList : ArrayList<Types> = ArrayList()
    var recipeList : ArrayList<RecipesModel> = ArrayList()
    var type : String = "food"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinner = findViewById(R.id.types_spinner)
        rvRecipe = findViewById(R.id.rv_recipe)
        btnAdd = findViewById(R.id.btn_add)
        btnLogout = findViewById(R.id.btn_logout)
        rvRecipe.layoutManager = LinearLayoutManager(this)
        sqLiteHelper = SQLiteHelper(this)

        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE)
        sharedPreferences2 = getSharedPreferences("loggedIn", Context.MODE_PRIVATE)
        firebaseAuth = FirebaseAuth.getInstance()
        readXmlFile()

        btnAdd.setOnClickListener {
            val i = Intent(this, AddEditRecipe::class.java)
            startActivity(i)
        }

        btnLogout.setOnClickListener {
            val editor = sharedPreferences2.edit()
            editor.clear()
            editor.commit()

            firebaseAuth.signOut()
            val intent = Intent(this, LandingActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume(){
        super.onResume()
    }

    private fun readXmlFile() {
        val xml_data = assets.open("recipetypes.xml")
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()

        parser.setInput(xml_data, null)
        var event = parser.eventType
        while(event!=XmlPullParser.END_DOCUMENT){
            val tag_name = parser.name
            when(event){
                XmlPullParser.END_TAG -> {
                    if(tag_name == "type"){
                        val id = parser.getAttributeValue(0)
                        val name = parser.getAttributeValue(1)
                        val types = parser.getAttributeValue(2)
                        recipeTypeList.add(Types(id,name,types))
                        typeList.add(name)
                    }
                }
            }
            event = parser.next()
        }

        val adapter = ArrayAdapter(this, R.layout.tv_spinner, typeList)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                type = recipeTypeList[position].type
                fetchRecyclerView()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        fetchRecyclerView()
    }

    private fun fetchRecyclerView() {
        recipeList.clear()
        Log.d("prefs", sharedPreferences.getString("default", null).toString())
        if(sharedPreferences.getString("default", null) == null){
            val tempRecipeList : ArrayList<RecipesModel> = ArrayList()

            val xml_data = assets.open("recipetypes.xml")
            val factory = XmlPullParserFactory.newInstance()
            val parser= factory.newPullParser()
            parser.setInput(xml_data, null)
            var event2 = parser.eventType
            while(event2!=XmlPullParser.END_DOCUMENT){
                val tag_name = parser.name
                when(event2){
                    XmlPullParser.END_TAG -> {
                        if(tag_name == "recipe"){
                            val id = parser.getAttributeValue(0)
                            val type = parser.getAttributeValue(1)
                            val name = parser.getAttributeValue(2)
                            val ingredients = parser.getAttributeValue(3)
                            val steps = parser.getAttributeValue(4)
                            val image = parser.getAttributeValue(5)
                            tempRecipeList.add(RecipesModel(id, type, name, ingredients, steps, image))
                        }
                    }
                }
                event2 = parser.next()
            }

            for (x in 0 until tempRecipeList.count()){
                val rcp = RecipesModel(id = tempRecipeList[x].id, type = tempRecipeList[x].type ,name =  tempRecipeList[x].name, ingredients =  tempRecipeList[x].ingredients, steps =  tempRecipeList[x].steps, image = tempRecipeList[x].image)
                val status = sqLiteHelper.insertRecipe(rcp)
            }

            val editor = sharedPreferences.edit()
            editor.putString("default", "yes")
            editor.commit()
        }

        val rcpList = sqLiteHelper.getAllRecipe()
        Log.d("list", rcpList.toString())

        for (y in 0 until rcpList.count()){
            if(type == "all"){
                recipeList.add(RecipesModel(rcpList[y].id, rcpList[y].type, rcpList[y].name, rcpList[y].ingredients, rcpList[y].steps, rcpList[y].image))
            } else {
                if(rcpList[y].type == type){
                    recipeList.add(RecipesModel(rcpList[y].id, rcpList[y].type, rcpList[y].name, rcpList[y].ingredients, rcpList[y].steps, rcpList[y].image))
                }
            }
        }

        val rvAdapter = RecipeAdapter(this, recipeList)
        rvRecipe.adapter = rvAdapter
    }

    override fun onBackPressed() {
        val nothing = false
        if(nothing){
            super.onBackPressed()
        }
    }
}