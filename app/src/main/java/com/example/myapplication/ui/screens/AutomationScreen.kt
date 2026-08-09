package com.example.myapplication.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.data.Person
import com.example.myapplication.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationScreen(
    viewModel: MainViewModel,
    personId: Int,
    dailyPassCode: String,
    visitType: String,
    onNavigateBack: () -> Unit,
) {
    var person by remember { mutableStateOf<Person?>(null) }
    val vipCardNumber by viewModel.vipCardNumber.collectAsState()

    LaunchedEffect(personId) {
        Log.d("AutomationScreen", "Launched for personId: $personId")
        person = viewModel.getPersonById(personId)
        if (person == null) {
            Log.e("AutomationScreen", "Person not found for id: $personId")
        } else {
            Log.d("AutomationScreen", "Person fetched: ${person?.firstName} ${person?.lastName}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registration") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (person != null) {
                AutomationWebView(
                    person = person!!,
                    dailyPassCode = dailyPassCode,
                    visitType = visitType,
                    vipCardNumber = vipCardNumber,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AutomationWebView(
    person: Person,
    dailyPassCode: String,
    visitType: String,
    vipCardNumber: String,
    modifier: Modifier = Modifier
) {
    val url = "https://www.ggpx.info/GuestReg"
    var step by remember { mutableIntStateOf(1) }
    var isLoading by remember { mutableStateOf(value = true) }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                Log.d("AutomationWebView", "Creating WebView")
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.WHITE)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true

                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            consoleMessage?.let {
                                Log.d("AutomationWebView JS", it.message())
                            }
                            return true
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            Log.d("AutomationWebView", "onPageStarted: $url")
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            Log.d("AutomationWebView", "onPageFinished: $url, Step: $step")
                            isLoading = false

                            if (url?.lowercase()?.contains("guestreg") == true) {
                                if (step == 1) {
                                    Log.d("AutomationWebView", "Injecting Step 1 JS for gymId: ${person.preferredGymId}")
                                    val safeGymId = person.preferredGymId.replace("'", "\\'")
                                    val js = """
                                        (function() {
                                            console.log('Step 1: Gym Selection Starting');
                                            var gymDropdown = document.getElementById('GymID') || document.getElementById('ddlGym');
                                            var gymId = '$safeGymId';
                                            if (gymDropdown && gymId) {
                                                console.log('Setting GymID to: ' + gymId);
                                                gymDropdown.value = gymId;
                                                gymDropdown.dispatchEvent(new Event('change', { bubbles: true }));
                                                var btnSubmitGym = document.getElementById('btnSubmitGym');
                                                if (btnSubmitGym) {
                                                    console.log('Clicking btnSubmitGym');
                                                    btnSubmitGym.click();
                                                    return;
                                                }
                                            }
                                            if (document.getElementById('FirstName') || document.getElementById('RegCode')) {
                                                console.log('Already on Step 2. Moving step counter.');
                                                // Trigger logic for step 2 if elements exist
                                            }
                                        })();
                                    """.trimIndent()

                                    step = 2
                                    view?.evaluateJavascript(js, null)
                                } else if (step == 2) {
                                    Log.d("AutomationWebView", "Injecting Step 2 JS")
                                    val passType = if (visitType == "Free Trial") "GPC" else "GVM"
                                    val js = """
                                        (function() {
                                            console.log('Step 2: Main Form Filling Starting');
                                            
                                            function typeInto(id, value) {
                                                if (!value) return;
                                                var el = document.getElementById(id);
                                                if (!el) {
                                                    console.warn('Element with ID ' + id + ' not found');
                                                    return;
                                                }
                                                console.log('Typing into ' + id);
                                                el.value = '';
                                                var i = 0;
                                                function type() {
                                                    if (i < value.length) {
                                                        el.value += value.charAt(i);
                                                        el.dispatchEvent(new Event('input', { bubbles: true }));
                                                        i++;
                                                        setTimeout(type, 30);
                                                    } else {
                                                        el.dispatchEvent(new Event('change', { bubbles: true }));
                                                        console.log('Finished typing into ' + id);
                                                    }
                                                }
                                                type();
                                            }

                                            function check(id) {
                                                var el = document.getElementById(id);
                                                if (el) {
                                                    console.log('Checking checkbox: ' + id);
                                                    el.checked = true;
                                                    el.dispatchEvent(new Event('change', { bubbles: true }));
                                                }
                                            }

                                            // 1. Visit Type Selection
                                            var guestPassType = document.getElementById('GuestPassType');
                                            if (guestPassType) {
                                                console.log('Setting Visit Type to: $passType');
                                                guestPassType.value = '$passType';
                                                guestPassType.dispatchEvent(new Event('change', { bubbles: true }));
                                                if (typeof EnableDisablePromoCode === 'function') {
                                                    EnableDisablePromoCode();
                                                }
                                            }

                                            // 2. Typing Simulator
                                            typeInto('RegCode', '${dailyPassCode.replace("'", "\\'")}');
                                            typeInto('FirstName', '${person.firstName.replace("'", "\\'")}');
                                            typeInto('LastName', '${person.lastName.replace("'", "\\'")}');
                                            typeInto('Email', '${person.email.replace("'", "\\'")}');
                                            typeInto('YearOfBirth', '${if (person.yearOfBirth > 0) person.yearOfBirth.toString() else ""}');
                                            typeInto('StreetAddress', '${person.address.replace("'", "\\'")}');
                                            typeInto('Appartment', '${person.apartment.replace("'", "\\'")}');
                                            typeInto('City', '${person.city.replace("'", "\\'")}');
                                            typeInto('StateProv', '${person.stateProv.replace("'", "\\'")}');
                                            typeInto('PostalCode', '${person.postalCode.replace("'", "\\'")}');
                                            typeInto('PhoneMobile', '${person.phone.replace("'", "\\'")}');
                                            
                                            if ('${person.gender}' === 'M') check('GenderM');
                                            else if ('${person.gender}' === 'F') check('GenderF');
                                            
                                            // 3. VIP / Reference Number
                                            if ('$vipCardNumber') {
                                                console.log('Filling PromoCode field');
                                                typeInto('PromoCode', '$vipCardNumber');
                                            }
                                            
                                            // 4. Agreements
                                            check('GuestServicesAgreement1');
                                            check('GuestServicesAgreement2');
                                            check('GuestServicesAgreement3');
                                            check('Agreement');
                                            
                                            var btnSubmit = document.getElementById('btnSubmit');
                                            if (btnSubmit) {
                                                console.log('Scrolling submit button into view');
                                                btnSubmit.scrollIntoView();
                                            }
                                        })();
                                    """.trimIndent()

                                    view?.evaluateJavascript(js, null)
                                }
                            }
                        }
                    }
                    loadUrl(url)
                }
            }
        ) {
            // Keep empty
        }

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().align(androidx.compose.ui.Alignment.TopCenter)
            )
        }
    }
}
