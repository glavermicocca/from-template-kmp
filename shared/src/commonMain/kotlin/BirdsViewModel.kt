import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.BirdImage

data class BirdsUiState(
    val images: List<BirdImage> = emptyList(),
    val selectedCategory: String? = null
){
    val imagesCount: Int
        get() = images.size
    val categories: Set<String> = images.map { it.category }.toSet()
    val selectedImages: List<BirdImage> = images.filter { it.category == selectedCategory }
}

class BirdsViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(BirdsUiState())
    val uiState: StateFlow<BirdsUiState> = _uiState.asStateFlow()

    init {
        updateImages()
    }

    fun selectCategory(category: String) {
        _uiState.update {
            it.copy(selectedCategory = category)
        }
    }

    fun updateImages() {
        viewModelScope.launch {
            val images = getImages()
            _uiState.update {
                it.copy(images = images)
            }
        }
    }

    override fun onCleared() {
        httpClient.close()
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    private suspend fun getImages(): List<BirdImage> {
        val images = httpClient.get("https://sebi.io/demo-image-api/pictures.json")
            .body<List<BirdImage>>()
        return images
    }
}