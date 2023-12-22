package my.tutorials.recipeapp

data class RecipesModel(
    var id: String = "",
    var type: String = "",
    var name: String = "",
    var ingredients : String = "",
    var steps: String = "",
    var image: String = ""
)