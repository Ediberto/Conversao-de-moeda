package com.ediberto.conversao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    suspend fun getBRLToUSD(): Map<String, ExchangeRateDetail>

    @GET("EUR-BRL")
    suspend fun getBRLToEUR(): Map<String, ExchangeRateDetail>
}

// Classe de resposta da API
data class ExchangeRateDetail(
    val bid: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionScreen() {
    var inputValue by remember { mutableStateOf(TextFieldValue("")) }
    var convertedValueUSD by remember { mutableStateOf("") }
    var convertedValueEUR by remember { mutableStateOf("") }
    var exchangeRateUSD by remember { mutableStateOf("") }
    var exchangeRateEUR by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Criação do Retrofit apenas uma vez
    val retrofit = Retrofit.Builder()
        .baseUrl("https://economia.awesomeapi.com.br/json/last/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service = retrofit.create(ExchangeRateApi::class.java)
    val scope = rememberCoroutineScope()

    // Obtenha a data atual
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Conversão em tempo real de Moeda",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Autor: Ediberto",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 19.sp),
            fontWeight = FontWeight.Bold,
            color = Color.Blue
        )
        Text(
            text = "Data: $currentDate",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
            fontWeight = FontWeight.Bold,
            color = Color.Magenta
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Informe o valor em Real - R$",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = inputValue,
            onValueChange = { newValue -> inputValue = newValue },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            textStyle = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Resultados das conversões
        Text(
            text = "Valor convertido em:",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
            fontWeight = FontWeight.Bold,
            color = Color.Blue
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Dólar - US$: $convertedValueUSD",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Euro - €: $convertedValueEUR",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(22.dp))

        // Taxas de câmbio
        if (exchangeRateUSD.isNotEmpty()) {
            Text(
                text = "Taxa de câmbio Dólar: 1 USD = $exchangeRateUSD BRL",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
        if (exchangeRateEUR.isNotEmpty()) {
            Text(
                text = "Taxa de câmbio Euro: 1 EUR = $exchangeRateEUR BRL",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
        Spacer(modifier = Modifier.height(26.dp))

        // Botões de Converter e Limpar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = {
                val inputAmount = inputValue.text.toDoubleOrNull()
                if (inputAmount != null) {
                    scope.launch {
                        try {
                            val responseUSD = service.getBRLToUSD()
                            val rateUSD = responseUSD["USDBRL"]?.bid?.toDoubleOrNull() ?: 0.0
                            val responseEUR = service.getBRLToEUR()
                            val rateEUR = responseEUR["EURBRL"]?.bid?.toDoubleOrNull() ?: 0.0
                            withContext(Dispatchers.Main) {
                                convertedValueUSD = String.format("%.2f", inputAmount / rateUSD)
                                convertedValueEUR = String.format("%.2f", inputAmount / rateEUR)
                                exchangeRateUSD = String.format("%.2f", rateUSD)
                                exchangeRateEUR = String.format("%.2f", rateEUR)
                                errorMessage = ""
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                errorMessage = "Erro ao obter taxas de câmbio: ${e.message}"
                            }
                        }
                    }
                } else {
                    errorMessage = "Por favor, insira um valor numérico válido"
                }
            }) {
                Text("Converter", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Yellow)
            }
            Spacer(modifier = Modifier.width(16.dp)) // Espaço entre os botões
            Button(onClick = {
                // Limpa os dados
                inputValue = TextFieldValue("")
                convertedValueUSD = ""
                convertedValueEUR = ""
                exchangeRateUSD = ""
                exchangeRateEUR = ""
            }) {
                Text("Limpar Dados", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
            }
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


/* SEM A DATA DE HOJE
package com.ediberto.conversao
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
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
    suspend fun getBRLToUSD(): Map<String, ExchangeRateDetail>

    @GET("EUR-BRL")
    suspend fun getBRLToEUR(): Map<String, ExchangeRateDetail>
}
// Classe de resposta da API
data class ExchangeRateDetail(
    val bid: String
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionScreen() {
    var inputValue by remember { mutableStateOf(TextFieldValue("")) }
    var convertedValueUSD by remember { mutableStateOf("") }
    var convertedValueEUR by remember { mutableStateOf("") }
    var exchangeRateUSD by remember { mutableStateOf("") }
    var exchangeRateEUR by remember { mutableStateOf("") }
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
            text = "Conversão em tempo real de Moeda",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Autor: Ediberto",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 19.sp),
            fontWeight = FontWeight.Bold,
            color = Color.Blue
        )
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = "Informe o valor em Real - R$",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = inputValue,
            onValueChange = { newValue -> inputValue = newValue },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            textStyle = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        // Resultados das conversões
        Text(
            text = "Valor convertido em:",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
            fontWeight = FontWeight.Bold,
            color = Color.Blue
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Dólar - US$: $convertedValueUSD",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Euro - €: $convertedValueEUR",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(26.dp))
        // Taxas de câmbio
        if (exchangeRateUSD.isNotEmpty()) {
            Text(
                text = "Taxa de câmbio Dólar: 1 USD = $exchangeRateUSD BRL",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
        if (exchangeRateEUR.isNotEmpty()) {
            Text(
                text = "Taxa de câmbio Euro: 1 EUR = $exchangeRateEUR BRL",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
        Spacer(modifier = Modifier.height(26.dp))
        // Botões de Converter e Limpar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = {
                val inputAmount = inputValue.text.toDoubleOrNull()
                if (inputAmount != null) {
                    scope.launch {
                        try {
                            val responseUSD = service.getBRLToUSD()
                            val rateUSD = responseUSD["USDBRL"]?.bid?.toDoubleOrNull() ?: 0.0
                            val responseEUR = service.getBRLToEUR()
                            val rateEUR = responseEUR["EURBRL"]?.bid?.toDoubleOrNull() ?: 0.0
                            withContext(Dispatchers.Main) {
                                convertedValueUSD = String.format("%.2f", inputAmount / rateUSD)
                                convertedValueEUR = String.format("%.2f", inputAmount / rateEUR)
                                exchangeRateUSD = String.format("%.2f", rateUSD)
                                exchangeRateEUR = String.format("%.2f", rateEUR)
                                errorMessage = ""
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                errorMessage = "Erro ao obter taxas de câmbio: ${e.message}"
                            }
                        }
                    }
                } else {
                    errorMessage = "Por favor, insira um valor numérico válido"
                }
            }) {
                Text("Converter", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Cyan)
            }
            Spacer(modifier = Modifier.width(16.dp)) // Espaço entre os botões
            Button(onClick = {
                // Limpa os dados
                inputValue = TextFieldValue("")
                convertedValueUSD = ""
                convertedValueEUR = ""
                exchangeRateUSD = ""
                exchangeRateEUR = ""
            }) {
                Text("Limpar Dados", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Red)
            }
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
 */
