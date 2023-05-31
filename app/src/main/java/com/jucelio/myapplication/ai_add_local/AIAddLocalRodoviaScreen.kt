package com.jucelio.myapplication.ai_add_local


import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import br.gov.sp.artesp.sisftc.mobile.kotlin.data.model.Municipio
import br.gov.sp.artesp.sisftc.mobile.kotlin.data.model.Rodovia
import br.gov.sp.artesp.sisftc.mobile.kotlin.data.repository.fake.*
import br.gov.sp.artesp.sisftc.mobile.kotlin.ui.component.DialogOpenAssinatura
import br.gov.sp.artesp.sisftc.mobile.kotlin.ui.navigation.MainNavigationConstants
import br.gov.sp.artesp.sisftc.mobile.kotlin.ui.screen.ai.ai_add_irregularidade.AIAddIrregularidadeViewModel
import br.gov.sp.artesp.sisftc.mobile.kotlin.ui.screen.ai.ai_add_local.common.CommonDatePiker
import br.gov.sp.artesp.sisftc.mobile.kotlin.ui.screen.ai.ai_add_local.common.CommonExposedDropdownMenuBox
import br.gov.sp.artesp.sisftc.mobile.kotlin.ui.screen.ai.ai_add_local.common.CommonOutlinedTextField
import br.gov.sp.artesp.sisftc.mobile.kotlin.ui.screen.ai.ai_home.AIHomeViewModel
import br.gov.sp.artesp.sisftc.mobile.kotlin.ui.screen.notificacao.notificacao_add.common.ItemDropDow
import br.gov.sp.artesp.sisftc.mobile.kotlin.ui.screen.notificacao.notificacao_add.common.dropDownComponent
import br.gov.sp.artesp.sisftc.mobile.kotlin.utils.formatString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.AIAddLocalRodoviaScreen(
    navController: NavController,
    viewModel: AIAddLocalViewModel = hiltViewModel()
) {
    var openAssinaturaScreen by remember{ mutableStateOf(false) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val rodovias by viewModel.rodovias.observeAsState()

    val  aiviewModel:AIAddIrregularidadeViewModel =
        hiltViewModel()
    val listMunicipio by aiviewModel.listaMunicipios().observeAsState()
    Log.d("listaMunicipios", listMunicipio.toString())

    var dataEHora by rememberSaveable { mutableStateOf("") }
    var dataEHoraErrorEnabled by rememberSaveable { mutableStateOf(false) }

    val inputDateFormat = SimpleDateFormat("ddMMyyyyHHmm")
    val outputDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")


    var km by rememberSaveable { mutableStateOf("") }
    var kmErrorEnabled by rememberSaveable { mutableStateOf(false) }

    val fiscal by viewModel.fiscal.observeAsState()

    val coroutineScope = rememberCoroutineScope()

    var rodovia by rememberSaveable { mutableStateOf("") }
    var rodoviaExpanded by rememberSaveable { mutableStateOf(false) }
    var rodoviaErrorEnabled by rememberSaveable { mutableStateOf(false) }

    var municipio by  rememberSaveable { mutableStateOf("") }
    var municipioExpanded by rememberSaveable { mutableStateOf(false) }
    var municipioErrorEnabled by rememberSaveable { mutableStateOf(false) }



    var observacoes by rememberSaveable { mutableStateOf("") }

    if (municipio.isNotEmpty()) {
        municipioErrorEnabled = false
    }

    if (rodovia.isNotEmpty()) {
        rodoviaErrorEnabled = false
    }

    if (km.isNotEmpty()) {
        kmErrorEnabled = false
    }

    if (dataEHora.isNotEmpty()) {
        dataEHoraErrorEnabled = false
    }

    var inputDate: Date
    var outputDateStr: String = ""
    LaunchedEffect(true) {
        val dateFormatted = SimpleDateFormat("ddMMyyyyHHmm", Locale.getDefault()).format(Date())
        dataEHora = dateFormatted

        viewModel.buscarRodovias("")
    }


    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        // Data e Hora
        CommonDatePiker(value = dataEHora, isError = dataEHoraErrorEnabled, focusManager = focusManager, onChange = { value ->
            if (dataEHora.length <= 12) {
                dataEHora = value.toString()
            }
        })

        // Km
        CommonOutlinedTextField(label = "Km", value = km, isError = kmErrorEnabled, focusManager = focusManager, keyboardType = "text", onChange = { value ->
            km = value.toString()
        })


    }

    // Fiscal
    Column {
        Text(text = "Fiscal", style = MaterialTheme.typography.labelSmall)
        Text(text = fiscal?.Nmfiscal.toString(), style = MaterialTheme.typography.bodySmall)
    }
   //componente municipio esta com o alto complite
    //Municipio
    dropDownComponent(
        label = "Municipio",
        value = municipio,
        listOptions = listMunicipio?.map {
            ItemDropDow(
                text = it.nome,
                onClick = { text,_,_ ->
                    municipio = text
                }
            )
        }?: emptyList()
    )


    // Rodovia
    CommonExposedDropdownMenuBox(
        isExpanded = rodoviaExpanded,
        isErrorEnabled = rodoviaErrorEnabled,
        value = rodovia,
        label = "Rodovia",
        listOptions = rodovias ?: emptyList(),
        onValueChange = { value ->
            rodovia = value

            viewModel.buscarRodovias(rodovia)

            rodoviaExpanded = true
        }, getSelectedOption = { selectionOption ->
            val selection: Rodovia = selectionOption as Rodovia
            rodovia = selection.Nmrodovia.toString()

        }, setText = { value ->
            (value as Rodovia).Nmrodovia.toString()
        })



    // Observações
    CommonOutlinedTextField(label = "Observações", value = observacoes, focusManager = focusManager, keyboardType = "text", onChange = { value ->
        observacoes = value.toString()
    })

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        modifier = Modifier
            .width(300.dp)
            .align(Alignment.CenterHorizontally),
        onClick = {

            municipioErrorEnabled = false
            dataEHoraErrorEnabled = false
            kmErrorEnabled = false
            rodoviaErrorEnabled = false

            if (dataEHora.isEmpty()) {
                dataEHoraErrorEnabled = true
                return@Button
            }

            if (km.isEmpty()) {
                kmErrorEnabled = true
                return@Button
            }
            if (municipio.isEmpty()){
                municipioErrorEnabled = true
                return@Button
            }

            if (rodovia.isEmpty()) {
                rodoviaErrorEnabled = true
                return@Button
            }
            try {
                inputDate = inputDateFormat.parse(dataEHora) as Date
                outputDateStr = outputDateFormat.format(inputDate)
                openAssinaturaScreen = true
            } catch (e: Exception) {
                dataEHoraErrorEnabled = true
                Toast.makeText(
                    context,
                    "Data e Hora inválida. Formato: 28/04/2023 06:43",
                    Toast.LENGTH_SHORT
                ).show()
                return@Button
            }
        }
    ) {
        Text(text = "Próximo")
    }
    Spacer(modifier = Modifier.height(16.dp))
    DialogOpenAssinatura(show = openAssinaturaScreen, onCancell = { openAssinaturaScreen = false }) {
        coroutineScope.launch(Dispatchers.IO) {
            if(outputDateStr.isEmpty())
                outputDateStr = Date().formatString("yyyy-MM-dd HH:mm")
            val aiTmp = viewModel.getLastAITmp()
            aiTmp.dataLavratura = outputDateStr
            aiTmp.fiscal = fiscal?.Nmfiscal.toString()
            aiTmp.fiscalCode = fiscal?.CdFiscal?.toInt()
            aiTmp.observacoes = observacoes
            aiTmp.rodovia = rodovia
            aiTmp.local = "Rodovia"
            aiTmp.km = km

            viewModel.updateAITmp(
                aiTmp
            )

            viewModel.generateAITmpFinalize()
        }
        navController.navigate(MainNavigationConstants.AI_ADD_ASSINATURA)
    }
}

@Composable
@Preview(device = Devices.PHONE, showBackground = true)
fun AIAddLocalRodoviaScreenPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AIAddLocalRodoviaScreen(
            navController = rememberNavController(),
            viewModel = AIAddLocalViewModel(
                rodoviaRepository = RodoviaRepositoryFake(),
                terminalRepository = TerminalRepositoryFake(),
                aiTmpRepository = AITmpRepositoryFake(),
                irregularidadeTMPRepository = IrregularidadeTMPRepositoryFake(),
                aiTmpFinalizeRepository = AITmpFinalizeRepositoryFake(),
                fiscalRepository = FiscalRepositoryFake(),
                nraiRepository = NraiRepositoryFake(),
            )
        )
    }
}