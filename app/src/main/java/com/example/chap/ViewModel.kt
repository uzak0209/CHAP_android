import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// UI状態を表すデータクラス
data class UiState(
    val data: String = "Initial Data",
    val isLoading: Boolean = false,
    val error: String? = null
)

class MyViewModel : ViewModel() {

    // UI状態を保持するプライベートなMutableStateFlow
    // 初期状態を指定する必要がある
    private val _uiState = MutableStateFlow(UiState())

    // UIに公開する読み取り専用のStateFlow
    // asStateFlow() を使用して、外部からの変更を防ぐ
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // 状態を更新する関数
    fun fetchData() {
        viewModelScope.launch {
            // ローディング状態を開始
            _uiState.update { currentState ->
                currentState.copy(isLoading = true)
            }

            try {
                // ここで実際のデータ取得処理を実行（例: ネットワークリクエスト）
                val result = "Updated Data after fetch" // ダミーデータ

                // 成功したらデータを更新
                _uiState.update { currentState ->
                    currentState.copy(data = result, isLoading = false)
                }
            } catch (e: Exception) {
                // エラーが発生したらエラーメッセージを更新
                _uiState.update { currentState ->
                    currentState.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    fun resetError() {
        _uiState.update { currentState ->
            currentState.copy(error = null)
        }
    }
}