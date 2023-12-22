package my.tutorials.recipeapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.text.SimpleDateFormat
import java.util.Locale

class AddEditRecipe : AppCompatActivity(){
    private lateinit var etName: AppCompatEditText
    private lateinit var spinner: Spinner
    private lateinit var etIngredients: AppCompatEditText
    private lateinit var etSteps: AppCompatEditText
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button
    private lateinit var btnBack: Button
    private lateinit var sqLiteHelper: SQLiteHelper
    private lateinit var image: ImageView
    private lateinit var imageUri : Uri

    var isEditMode : Boolean = false
    var type : String = "food"
    var typeList: ArrayList<String> = arrayListOf("Food","Beverage")
    var recipeTypeList: ArrayList<Types> = ArrayList()
    var recipeId : String = ""
    var fileName : String = ""
    var newImageUri : String = ""

    val IMAGE_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_recipe)

        init()

        if(intent != null && intent.getStringExtra("Mode") == "E"){
            isEditMode = true
            btnSave.text = "Update Recipe"
            btnDelete.visibility = View.VISIBLE
            recipeId = intent.getStringExtra("Id")!!

            val recipe : RecipesModel = sqLiteHelper.getRecipe(recipeId.toInt())
            etName.setText(recipe.name)

            if(recipe.type == "food"){
                spinner.setSelection(0)
            } else {
                spinner.setSelection(1)
            }
            etIngredients.setText(recipe.ingredients)
            etSteps.setText(recipe.steps)

            newImageUri = recipe.image
            imageUri = Uri.parse(recipe.image)
            Picasso.get().load(imageUri).resize(200,200).centerCrop().into(image)
        } else {
            isEditMode = false
            btnSave.text = "Save Recipe"
            btnDelete.visibility = View.GONE
        }

        image.setOnClickListener {
            getImage()
        }

        btnSave.setOnClickListener {
            handleSaveBtn()
        }

        btnDelete.setOnClickListener {
            handleDeleteBtn()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK){
            imageUri = data?.data!!
            Picasso.get().load(imageUri).resize(200,200).centerCrop().into(image)
            uploadImage(imageUri)
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_LONG).show()
        }
    }

    private fun getImage(){
        ImagePicker.Companion.with(this)
            .cropSquare()
            .galleryOnly()
            .maxResultSize(1080, 1080)
            .start(IMAGE_REQUEST_CODE)
    }

    private fun uploadImage(imageUri: Uri){
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val databaseReference = FirebaseDatabase.getInstance()
        fileName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.ENGLISH).format(System.currentTimeMillis())
        val storageReference = FirebaseStorage.getInstance().reference.child("images/$fileName")
        val uploadTask = storageReference.putFile(imageUri)

        val urlTask = uploadTask.continueWithTask { task ->
            if(!task.isSuccessful){
                task.exception?.let {
                    throw it
                }
            }
            storageReference.downloadUrl
        }.addOnCompleteListener { task ->
            if(task.isSuccessful){
                val downloadUri = task.result
                databaseReference.reference.child("images").child(fileName).setValue(downloadUri.toString()).addOnSuccessListener {
                    newImageUri = downloadUri.toString()
                    if(progressDialog.isShowing) progressDialog.dismiss()
                }
            }
        }
    }

    private fun handleDeleteBtn(){
        val dialog = AlertDialog.Builder(this).setTitle("Info").setMessage("Do you really want to delete the recipe?")
            .setPositiveButton("YES") { dialog, i ->
                val success = sqLiteHelper.deleteRecipeById(recipeId.toInt())
                if (success > -1) {
                    finish()
                }
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("NO") { dialog, i ->
                dialog.dismiss()
            }

        dialog.show()
    }

    private fun handleSaveBtn(){
        val rcpList = sqLiteHelper.getAllRecipe()
        val uniqueId = rcpList.count() + 1

        val type = type
        val name = etName.text.toString()
        val ingredients = etIngredients.text.toString()
        val steps = etSteps.text.toString()
        val image = newImageUri

        //validation
        if(type.count() > 0 && name.count() > 0 && ingredients.count() > 0 && steps.count() > 0 && image.count() > 0){
            if(isEditMode){
                val id = recipeId
                val rcp = RecipesModel(id = id, type = type, name = name, ingredients = ingredients, steps = steps, image = image)
                val status = sqLiteHelper.updateRecipe(rcp)

                if(status > -1){
                    val i = Intent(this, MainActivity::class.java)
                    startActivity(i)
                } else {
                    Toast.makeText(this, "Something went wrong. Please try again", Toast.LENGTH_LONG).show()
                }
            } else {
                val id = uniqueId.toString()
                val rcp = RecipesModel(id = id, type = type, name = name, ingredients = ingredients, steps = steps, image = image)
                val status = sqLiteHelper.insertRecipe(rcp)


                if(status > -1){
                    val i = Intent(this, MainActivity::class.java)
                    startActivity(i)
                } else {
                    Toast.makeText(this, "Something went wrong. Please try again", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show()
        }
    }

    private fun init(){
        etName = findViewById(R.id.et_name)
        spinner = findViewById(R.id.spinner_types)
        etIngredients = findViewById(R.id.et_ingredients)
        etSteps = findViewById(R.id.et_steps)
        btnSave = findViewById(R.id.btn_save)
        btnDelete = findViewById(R.id.btn_delete)
        btnBack = findViewById(R.id.btn_back)
        image = findViewById(R.id.iv_image)
        sqLiteHelper = SQLiteHelper(this)

        val xml_data = assets.open("recipetypes.xml")
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()

        parser.setInput(xml_data, null)
        var event = parser.eventType
        while(event!= XmlPullParser.END_DOCUMENT){
            val tag_name = parser.name
            when(event){
                XmlPullParser.END_TAG -> {
                    if(tag_name == "type"){
                        val id = parser.getAttributeValue(0)
                        val name = parser.getAttributeValue(1)
                        val types = parser.getAttributeValue(2)
                        recipeTypeList.add(Types(id,name,types))
                    }
                }
            }
            event = parser.next()
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, typeList)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                type = recipeTypeList[position + 1].type
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    override fun onBackPressed() {
        val nothing = false
        if(nothing){
            super.onBackPressed()
        }
    }
}