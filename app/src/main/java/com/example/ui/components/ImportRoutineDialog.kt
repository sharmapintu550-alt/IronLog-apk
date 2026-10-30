package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.entity.ExerciseEntity
import com.example.data.entity.RoutineEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutViewModel
import com.example.util.QrCodeGenerator
import com.example.util.SharedRoutineExercise
import com.example.util.SharedRoutinePayload
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.util.UUID
import java.util.concurrent.Executors

@Composable
fun ImportRoutineDialog(
    viewModel: WorkoutViewModel,
    initialPayload: SharedRoutinePayload? = null,
    onDismiss: () -> Unit,
    onRoutineImported: (RoutineEntity) -> Unit = {}
) {
    val context = LocalContext.current
    val allDbExercises by viewModel.exercises.collectAsState()

    var activeTab by remember { mutableIntStateOf(if (initialPayload != null) 1 else 0) } // 0: Scanner, 1: Paste/Manual
    var scannedPayload by remember { mutableStateOf<SharedRoutinePayload?>(initialPayload) }
    var pastedText by remember { mutableStateOf("") }

    // If payload is available, show the preview & save modal
    if (scannedPayload != null) {
        ImportRoutinePreviewModal(
            payload = scannedPayload!!,
            allDbExercises = allDbExercises,
            onDismiss = { scannedPayload = null },
            onSaveRoutine = { routine, customExercises ->
                viewModel.saveImportedRoutine(routine, customExercises) { success ->
                    if (success) {
                        Toast.makeText(context, "Routine imported successfully!", Toast.LENGTH_SHORT).show()
                        onRoutineImported(routine)
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Failed to save routine", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        return
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(AmoledBlack)
                .testTag("import_routine_dialog"),
            color = AmoledBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_import_dialog_button")) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Import Routine",
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Mode Switch Tabs (Camera / Paste)
                    Surface(
                        color = DarkCardBg,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Row(modifier = Modifier.padding(3.dp)) {
                            Surface(
                                color = if (activeTab == 0) ElectricBlue else Color.Transparent,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .testTag("tab_camera_scanner")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = if (activeTab == 0) TextWhite else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Scan",
                                        color = if (activeTab == 0) TextWhite else TextMuted,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Surface(
                                color = if (activeTab == 1) ElectricBlue else Color.Transparent,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .testTag("tab_paste_link")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ContentPaste,
                                        contentDescription = null,
                                        tint = if (activeTab == 1) TextWhite else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Paste Link",
                                        color = if (activeTab == 1) TextWhite else TextMuted,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                if (activeTab == 0) {
                    CameraScannerView(
                        onQrCodeScanned = { rawResult ->
                            val parsed = QrCodeGenerator.parseInput(rawResult)
                            if (parsed != null) {
                                scannedPayload = parsed
                            } else {
                                Toast.makeText(context, "Invalid IronLog routine QR code.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onSwitchToPaste = { activeTab = 1 }
                    )
                } else {
                    PasteLinkImportView(
                        pastedText = pastedText,
                        onTextChanged = { pastedText = it },
                        onParse = { textToParse ->
                            val parsed = QrCodeGenerator.parseInput(textToParse)
                            if (parsed != null) {
                                scannedPayload = parsed
                            } else {
                                Toast.makeText(context, "Invalid IronLog routine QR code.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CameraScannerView(
    onQrCodeScanned: (String) -> Unit,
    onSwitchToPaste: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission required to scan QR codes", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        // Fallback UI when camera permission is not granted
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = DarkCardBg,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "Camera Access Needed",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Grant camera permission to scan IronLog routine QR codes, or switch to pasting a shared link.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onSwitchToPaste,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderHover),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                        ) {
                            Text("Paste Link")
                        }
                        Button(
                            onClick = { launcher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                        ) {
                            Text("Grant Access")
                        }
                    }
                }
            }
        }
        return
    }

    // Live Camera Preview with Scanner Reticle Overlay
    val lifecycleOwner = LocalLifecycleOwner.current
    var isScannedLocked by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("camera_scanner_view")
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                val cameraExecutor = Executors.newSingleThreadExecutor()

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor, MlKitBarcodeAnalyzer { rawText ->
                                if (!isScannedLocked) {
                                    isScannedLocked = true
                                    ContextCompat.getMainExecutor(ctx).execute {
                                        onQrCodeScanned(rawText)
                                    }
                                }
                            })
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalyzer
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // Dark Semi-transparent vignette and central Electric Blue Reticle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "scanner_anim")
            val reticleAlpha by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "reticle_alpha"
            )

            // Central Scanning Window
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, ElectricBlue.copy(alpha = reticleAlpha), RoundedCornerShape(24.dp))
            ) {
                // Corner Accent Brackets
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Box(modifier = Modifier.size(20.dp).align(Alignment.TopStart).border(3.dp, ElectricBlue, RoundedCornerShape(topStart = 8.dp)))
                    Box(modifier = Modifier.size(20.dp).align(Alignment.TopEnd).border(3.dp, ElectricBlue, RoundedCornerShape(topEnd = 8.dp)))
                    Box(modifier = Modifier.size(20.dp).align(Alignment.BottomStart).border(3.dp, ElectricBlue, RoundedCornerShape(bottomStart = 8.dp)))
                    Box(modifier = Modifier.size(20.dp).align(Alignment.BottomEnd).border(3.dp, ElectricBlue, RoundedCornerShape(bottomEnd = 8.dp)))
                }
            }
        }

        // Bottom instruction banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(AmoledBlack.copy(alpha = 0.85f))
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Point camera at an IronLog routine QR code",
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            TextButton(
                onClick = onSwitchToPaste,
                modifier = Modifier.testTag("switch_to_paste_button")
            ) {
                Text(
                    text = "Or paste link / text instead",
                    color = ElectricBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun PasteLinkImportView(
    pastedText: String,
    onTextChanged: (String) -> Unit,
    onParse: (String) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            color = DarkCardBg,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Paste Shared Routine Data",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Paste a deep link (ironlog://routine/share?data=...) or raw routine JSON below to import it into your routines.",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = pastedText,
                    onValueChange = onTextChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("paste_link_input"),
                    placeholder = {
                        Text(
                            text = "ironlog://routine/share?data=...\nOR\n{\"routine_name\": \"Leg Day\", ...}",
                            color = TextDark,
                            fontSize = 12.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkCardSurface,
                        unfocusedContainerColor = DarkCardSurface
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = clipboard.primaryClip
                            if (clip != null && clip.itemCount > 0) {
                                val text = clip.getItemAt(0).text?.toString() ?: ""
                                onTextChanged(text)
                            } else {
                                Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderHover),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paste Clipboard", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (pastedText.isNotBlank()) {
                                onParse(pastedText)
                            } else {
                                Toast.makeText(context, "Please paste link or JSON data first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("parse_pasted_data_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Text("Parse & Review", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ImportRoutinePreviewModal(
    payload: SharedRoutinePayload,
    allDbExercises: List<ExerciseEntity>,
    onDismiss: () -> Unit,
    onSaveRoutine: (RoutineEntity, List<ExerciseEntity>) -> Unit
) {
    var routineTitle by remember { mutableStateOf(payload.routineName) }
    var routineDesc by remember { mutableStateOf(payload.description) }

    // Resolve exercises against local catalog
    val resolvedExercises = remember(payload, allDbExercises) {
        payload.exercises.map { sharedEx ->
            val matchedInDb = allDbExercises.find {
                it.id == sharedEx.id || it.name.equals(sharedEx.name, ignoreCase = true)
            }
            val isCustomNeeded = matchedInDb == null
            val finalName = matchedInDb?.name ?: sharedEx.name
            val finalMuscle = matchedInDb?.primaryMuscle?.takeIf { it.isNotBlank() }
                ?: matchedInDb?.muscleGroup?.takeIf { it.isNotBlank() }
                ?: sharedEx.muscle
            val finalEquipment = matchedInDb?.equipmentType?.takeIf { it.isNotBlank() }
                ?: matchedInDb?.equipment?.takeIf { it.isNotBlank() }
                ?: sharedEx.equipment

            ResolvedExerciseItem(
                sharedExercise = sharedEx,
                resolvedName = finalName,
                resolvedMuscle = finalMuscle,
                resolvedEquipment = finalEquipment,
                isCustomToInsert = isCustomNeeded
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(AmoledBlack)
                .testTag("import_routine_preview_modal"),
            color = AmoledBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Import Routine Preview",
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${resolvedExercises.size} Exercises Found",
                            color = ElectricBlueLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("cancel_import_routine_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Editable Title Field
                OutlinedTextField(
                    value = routineTitle,
                    onValueChange = { routineTitle = it },
                    label = { Text("Routine Name", color = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("import_routine_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkCardBg,
                        unfocusedContainerColor = DarkCardBg
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "EXERCISES INCLUDED",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // List of Resolved Exercises
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(resolvedExercises) { item ->
                        Surface(
                            color = DarkCardBg,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.resolvedName,
                                        color = TextWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = item.resolvedMuscle,
                                            color = ElectricBlueLight,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "• ${item.sharedExercise.defaultSets} Sets × ${item.sharedExercise.targetReps}",
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                if (item.isCustomToInsert) {
                                    Surface(
                                        color = ElectricBlueAlpha,
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = "New Custom",
                                            color = ElectricBlueLight,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Surface(
                                        color = DarkCardSurface,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Catalog",
                                            color = StreakGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("cancel_import_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderHover),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                    ) {
                        Text("Cancel", fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            val summary = resolvedExercises.joinToString(", ") { it.resolvedName }
                            val newRoutine = RoutineEntity(
                                id = "routine_" + UUID.randomUUID().toString().take(8),
                                title = routineTitle.ifBlank { "Imported Routine" },
                                description = routineDesc,
                                exercisesSummary = summary,
                                exercisesJson = QrCodeGenerator.serializeToJson(payload),
                                sortOrder = 0,
                                createdAt = System.currentTimeMillis()
                            )

                            val customExercisesToInsert = resolvedExercises
                                .filter { it.isCustomToInsert }
                                .map { item ->
                                    ExerciseEntity(
                                        id = "custom_" + UUID.randomUUID().toString().take(8),
                                        name = item.resolvedName,
                                        muscleGroup = item.resolvedMuscle,
                                        equipment = item.resolvedEquipment,
                                        isCustom = true
                                    )
                                }

                            onSaveRoutine(newRoutine, customExercisesToInsert)
                        },
                        modifier = Modifier
                            .weight(2f)
                            .height(50.dp)
                            .testTag("save_imported_routine_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import Routine", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class ResolvedExerciseItem(
    val sharedExercise: SharedRoutineExercise,
    val resolvedName: String,
    val resolvedMuscle: String,
    val resolvedEquipment: String,
    val isCustomToInsert: Boolean
)

@OptIn(ExperimentalGetImage::class)
class MlKitBarcodeAnalyzer(
    private val onQrCodeFound: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    private var isAnalyzing = false

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null && !isAnalyzing) {
            isAnalyzing = true
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (!rawValue.isNullOrBlank()) {
                            onQrCodeFound(rawValue)
                            break
                        }
                    }
                }
                .addOnCompleteListener {
                    isAnalyzing = false
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}

