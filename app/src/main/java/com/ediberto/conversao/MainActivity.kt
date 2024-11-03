package com.ediberto.conversao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ediberto.meuapp.ui.theme.ConversaoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConversaoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ConversionScreen()
                }
            }
        }
    }
}

// Interface para a API de câmbio
interface ExchangeRateApi {
    @GET("USD-BRL")
    suspend fun getExchangeRate(): Map<String, ExchangeRateDetail>
}

// Classe de resposta da API
data class ExchangeRateDetail(
    val bid: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionScreen() {
    var inputValue by remember { mutableStateOf(TextFieldValue("")) }
    var convertedValue by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Criação do Retrofit apenas uma vez
    val retrofit = Retrofit.Builder()
        .baseUrl("https://economia.awesomeapi.com.br/json/last/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(ExchangeRateApi::class.java)
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Conversão de Moeda",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Autor: Ediberto",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Informe o valor em Real",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = inputValue,
            onValueChange = { newValue -> inputValue = newValue },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Valor convertido: $convertedValue",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            val inputAmount = inputValue.text.toDoubleOrNull()
            if (inputAmount != null) {
                scope.launch {
                    try {
                        val response = service.getExchangeRate()
                        val rate = response["USDBRL"]?.bid?.toDoubleOrNull() ?: 0.0

                        withContext(Dispatchers.Main) {
                            // Divisão do valor em reais pela taxa para obter o valor em dólares
                            convertedValue = String.format("%.2f", inputAmount / rate)
                            errorMessage = ""
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            errorMessage = "Erro ao obter taxa de câmbio: ${e.message}"
                        }
                    }
                }
            } else {
                errorMessage = "Por favor, insira um valor numérico válido"
            }
        }) {
            Text("Converter para Dólar")
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Preview
@Composable
fun ConversionScreenPreview() {
    ConversaoTheme {
        ConversionScreen()
    }
}
/*package com.ediberto.conversao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ediberto.meuapp.ui.theme.ConversaoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConversaoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ConversionScreen()
                }
            }
        }
    }
}

// Interface para a API de câmbio
interface ExchangeRateApi {
    @GET("USD-BRL")
    suspend fun getExchangeRate(
        @Query("app_id") apiKey: String // Chave da API como um parâmetro de consulta
    ): ExchangeRateResponse
}
// Classe de resposta da API
data class ExchangeRateResponse(
    val USD: ExchangeRateDetail
)

data class ExchangeRateDetail(
    val bid: String
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionScreen() {
    var inputValue by remember { mutableStateOf(TextFieldValue("")) }
    var convertedValue by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    // Criação do Retrofit apenas uma vez

    val retrofit = Retrofit.Builder()
        .baseUrl("https://docs.awesomeapi.com.br/api-de-moedas/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(ExchangeRateApi::class.java)

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Conversão de Moeda",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Autor: Ediberto",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Informe o valor em Real",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = inputValue,
            onValueChange = { newValue -> inputValue = newValue },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Valor convertido: $convertedValue",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        val scope = rememberCoroutineScope()
        Button(onClick = {
            val inputAmount = inputValue.text.toDoubleOrNull()
            if (inputAmount != null) {
                scope.launch {
                    try {
                        val apiKey = "15907|OzL7GjbRVzTHNitnfsH6e3rTVQPW05dX"  // Defina sua chave da API aqui
                        val response = service.getExchangeRate(apiKey)  // Passe a chave da API

                        val rate = response.USD.bid.toDoubleOrNull() ?: 0.0

                        withContext(Dispatchers.Main) {
                            convertedValue = String.format("%.2f", inputAmount * rate)
                            errorMessage = ""
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            errorMessage = "Erro ao obter taxa de câmbio: ${e.message}"
                        }
                    }
                }
            } else {
                errorMessage = "Por favor, insira um valor numérico válido"
            }
        }) {
            Text("Converter para Dólar")
        }


        Spacer(modifier = Modifier.height(8.dp))
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}
*/