package com.ph.processchecker.page

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ph.processchecker.R
import com.ph.processchecker.ext.getDate
import com.ph.processchecker.ext.hasUsageStatsPermission
import com.ph.processchecker.ext.logD
import com.ph.processchecker.ext.startListenPermissionGranted
import com.ph.processchecker.ext.stopListenPermissionGranted
import com.ph.processchecker.model.AppInfo
import com.ph.processchecker.ui.composable.AppImage
import com.ph.processchecker.ui.theme.AppTheme
import com.ph.processchecker.util.ProcessWatcher
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                MainPage()
            }
        }
    }
}

@Composable
fun MainPage(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    MainContent(uiState = uiState, modifier = modifier, handleUiIntent = {
        viewModel.sendUiIntent(it, scope)
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent(
    uiState: MainContract.UiState,
    modifier: Modifier = Modifier,
    handleUiIntent: (uiIntent: MainContract.UiIntent) -> Unit

) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LifecycleResumeEffect(Unit) {
        logD(TAG) { "Resume" }
        stopListenPermissionGranted()
        ProcessWatcher.stopWatch()
        handleUiIntent(MainContract.UiIntent.Refresh)
        onPauseOrDispose {
            logD(TAG) { "Pause" }
        }
    }
    val appSettingJumpLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
//            handleUiIntent(MainContract.UiIntent.Refresh)
        }
    )
    val usageStatsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
//            if (context.hasUsageStatsPermission()) {
//                handleUiIntent(MainContract.UiIntent.Refresh)
//            }
        }
    )
    val usageStatsPermissionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    LaunchedEffect(Unit) {
        if (!context.hasUsageStatsPermission()) {
            handleUiIntent(MainContract.UiIntent.ChangeShowUsageStatsPermissionBottomSheet(true))
        }
    }
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            stickyHeader {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(color = Color.White),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 18.sp,
                        fontWeight = FontWeight(500),
                        color = Color(0xFF1A1A1A)
                    )
                }
            }
            uiState.data.forEach {
                item {
                    AppItem(appInfo = it, onClick = {
                        appSettingJumpLauncher.launch(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = "package:${it.packageName}".toUri()
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                        ProcessWatcher.startWatch(
                            backActivity = MainActivity::class.java,
                            oldList = uiState.data,
                            targetApp = it,
                        )
                    })
                }
            }
        }
        if (uiState.showUsageStatsPermissionBottomSheet) {
            fun hideUsageStatsPermissionBottomSheet() {
                handleUiIntent(
                    MainContract.UiIntent.ChangeShowUsageStatsPermissionBottomSheet(
                        false
                    )
                )
            }
            ModalBottomSheet(
                onDismissRequest = ::hideUsageStatsPermissionBottomSheet,
                sheetState = usageStatsPermissionSheetState,
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                dragHandle = {},
                containerColor = Color.White
            ) {
                UsageStatsPermissionBottomSheetContent(onClick = {
                    usageStatsPermissionLauncher.launch(
                        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    )
                    context.startListenPermissionGranted(MainActivity::class.java)
                    scope.launch { usageStatsPermissionSheetState.hide() }.invokeOnCompletion {
                        if (!usageStatsPermissionSheetState.isVisible) {
                            hideUsageStatsPermissionBottomSheet()
                        }
                    }
                })
            }
        }
        AnimatedVisibility(uiState.loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun AppItem(appInfo: AppInfo, modifier: Modifier = Modifier, onClick: () -> Unit) {
    // remember 避免重组重复转换
    val imageBitmap = remember(appInfo.icon) {
        val drawable = appInfo.icon
        // 创建画布绘制Drawable
        val bitmap = createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1)
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap.asImageBitmap()
    }
    val lastUseTime = if (appInfo.lastUseTime <= 0) "无记录" else appInfo.lastUseTime.getDate()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(16.dp))
        AppImage(
            bitmap = imageBitmap,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(shape = RoundedCornerShape(10.dp))
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = appInfo.appName,
                fontSize = 16.sp,
                fontWeight = FontWeight(500),
                color = Color(0xFF1A1A1A),
                maxLines = 1,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = appInfo.packageName,
                fontSize = 12.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFF999999),
                maxLines = 1,
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = lastUseTime,
            fontSize = 12.sp,
            fontWeight = FontWeight(400),
            color = Color(0xFF999999)
        )
        Spacer(Modifier.width(16.dp))
    }
}

@Composable
private fun UsageStatsPermissionBottomSheetContent(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .height(4.dp)
                .width(40.dp)
                .background(color = Color(0xFFDBDBDB), shape = RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.height(20.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppImage(
                painter = painterResource(R.drawable.ic_usage_stats_permission),
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.usage_stats_permission_title),
                fontSize = 16.sp,
                fontWeight = FontWeight(500),
                color = Color(0xFF1A1A1A)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.usage_stats_permission_desc),
            fontSize = 12.sp,
            fontWeight = FontWeight(400),
            color = Color(0xFF999999)
        )
        Spacer(Modifier.height(24.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1462FA),
                contentColor = Color.White
            ),
            onClick = onClick
        ) {
            Text(
                text = stringResource(R.string.turn_on),
                fontSize = 16.sp,
                fontWeight = FontWeight(500),
                color = Color.White
            )
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0x000)
@Composable
private fun UsageStatsPermissionBottomSheetContentPreview() {
    AppTheme {
        UsageStatsPermissionBottomSheetContent(
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainPagePreview() {
    AppTheme {
        MainContent(
            uiState = MainContract.UiState(
                data = List(5) {
                    AppInfo(
                        appName = "App Name",
                        icon = Color.Red.toArgb().toDrawable(),
                        packageName = "com.example.app"
                    )
                }
            ),
            handleUiIntent = {}
        )
    }
}