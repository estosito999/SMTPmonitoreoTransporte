package com.example.smtpmonitoreo.ui.components

import android.content.Context
import android.graphics.Paint
import android.view.MotionEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smtpmonitoreo.data.model.RouteModel
import com.example.smtpmonitoreo.data.model.TransportUnit
import com.example.smtpmonitoreo.ui.map.RoutePathsViewModel
import com.example.smtpmonitoreo.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex
import com.example.smtpmonitoreo.BuildConfig

/** Punto fijo con etiqueta (por ejemplo A = origen, B = destino). */
data class MapPin(
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val color: Int
)

@Composable
fun SmtpMapView(
    units: List<TransportUnit>,
    routes: List<RouteModel>,
    modifier: Modifier = Modifier,
    selectedUnit: TransportUnit? = null,
    highlightedRouteId: String? = null,
    pins: List<MapPin> = emptyList(),
    followSelected: Boolean = false,
    onUnitSelected: (TransportUnit) -> Unit = {},
    pathsViewModel: RoutePathsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val paths by pathsViewModel.paths.collectAsStateWithLifecycle()
    val mapView = remember { createLaPazMapView(context) }

    // Ciclo de vida del MapView (necesario para que cargue y libere teselas)
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    // Pide los trazados por calles de las rutas visibles
    LaunchedEffect(routes) { pathsViewModel.ensurePaths(routes) }

    // Centra el mapa en la unidad seleccionada (y la sigue, si se pide)
    val followKey = if (followSelected) selectedUnit?.let { it.latitude to it.longitude } else null
    LaunchedEffect(selectedUnit?.id, followKey) {
        selectedUnit?.let { mapView.controller.animateTo(GeoPoint(it.latitude, it.longitude)) }
    }

    // Encuadra la ruta resaltada
    val highlightedPath = highlightedRouteId?.let { paths[it] }
    LaunchedEffect(highlightedRouteId, highlightedPath) {
        if (highlightedPath != null && highlightedPath.size >= 2) {
            val box = BoundingBox.fromGeoPoints(highlightedPath.map { GeoPoint(it.first, it.second) })
            mapView.zoomToBoundingBox(box, true, 90)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, CyberBorder, RoundedCornerShape(20.dp))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { view ->
                view.render(
                    units = units,
                    routes = routes,
                    paths = paths,
                    highlightedRouteId = highlightedRouteId,
                    pins = pins,
                    selectedId = selectedUnit?.id,
                    onUnitSelected = onUnitSelected
                )
            }
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            shape = RoundedCornerShape(10.dp),
            color = DarkBackground.copy(alpha = 0.85f),
            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CyberEmerald)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LA PAZ EN VIVO · ${units.count { it.isTransmitting }} UNIDADES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
    }
}

private fun createLaPazMapView(context: Context): MapView = MapView(context).apply {
    setTileSource(MapTilerDark)
    setMultiTouchControls(true)
    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
    isTilesScaledToDpi = true

    // Solo La Paz: sin alejarse demasiado ni salirse del área
    minZoomLevel = LaPazMap.MIN_ZOOM
    maxZoomLevel = LaPazMap.MAX_ZOOM
    setScrollableAreaLimitDouble(LaPazMap.bounds)
    controller.setZoom(LaPazMap.DEFAULT_ZOOM)
    controller.setCenter(LaPazMap.center)

    // El mapa vive dentro de pantallas con scroll: evita que el scroll robe los gestos
    setOnTouchListener { view, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                view.parent?.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                view.parent?.requestDisallowInterceptTouchEvent(false)
        }
        false
    }
}

private fun unitColor(unit: TransportUnit): Int = when {
    !unit.isTransmitting -> TextMuted.toArgb()
    unit.vehicleType == "Trufi" -> CyberViolet.toArgb()
    unit.vehicleType == "Bus" -> RoleAdminColor.toArgb()
    else -> CyberCyan.toArgb()
}

private fun MapView.render(
    units: List<TransportUnit>,
    routes: List<RouteModel>,
    paths: Map<String, List<Pair<Double, Double>>>,
    highlightedRouteId: String?,
    pins: List<MapPin>,
    selectedId: String?,
    onUnitSelected: (TransportUnit) -> Unit
) {
    val map = this
    val density = resources.displayMetrics.density

    overlays.clear()
    overlays.add(CopyrightOverlay(context)) // atribución obligatoria de OpenStreetMap

    // Rutas por calles (o línea recta mientras se calculan o si no hay red)
    routes.forEach { route ->
        val points = paths[route.id] ?: route.waypoints
        if (points.size < 2) return@forEach

        val base = runCatching { android.graphics.Color.parseColor(route.colorHex) }
            .getOrDefault(CyberCyan.toArgb())
        val highlighted = route.id == highlightedRouteId
        val dimmed = highlightedRouteId != null && !highlighted

        overlays.add(
            Polyline().apply {
                setPoints(points.map { GeoPoint(it.first, it.second) })
                outlinePaint.color = ColorUtils.setAlphaComponent(
                    base,
                    when {
                        dimmed -> 70
                        highlighted -> 255
                        else -> 170
                    }
                )
                outlinePaint.strokeWidth = (if (highlighted) 5f else 3f) * density
                outlinePaint.strokeCap = Paint.Cap.ROUND
            }
        )
    }

    // Puntos fijos (origen y destino)
    pins.forEach { pin ->
        overlays.add(
            Marker(map).apply {
                position = GeoPoint(pin.latitude, pin.longitude)
                icon = MarkerIcons.get(context, pin.label, pin.color, selected = false)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                setInfoWindow(null)
                title = pin.label
            }
        )
    }

    // Unidades: las que transmiten y, si se pidió, la seleccionada aunque esté detenida
    units
        .filter { it.isTransmitting || it.id == selectedId }
        .forEach { unit ->
            overlays.add(
                Marker(map).apply {
                    position = GeoPoint(unit.latitude, unit.longitude)
                    icon = MarkerIcons.get(
                        context,
                        lineLabel(unit.lineNumber),
                        unitColor(unit),
                        selected = unit.id == selectedId
                    )
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    setInfoWindow(null)
                    title = "Línea ${unit.lineNumber} | ${unit.plate}"
                    setOnMarkerClickListener { _, _ ->
                        onUnitSelected(unit)
                        true
                    }
                }
            )
        }

    invalidate()
}
private val MapTilerDark = object : OnlineTileSourceBase(
    "MapTilerDarkSMTP", 0, 19, 256, ".png",
    arrayOf("https://api.maptiler.com/maps/streets-v2-dark/256/"),
    "© MapTiler © OpenStreetMap contributors"
) {
    override fun getTileURLString(pMapTileIndex: Long): String =
        baseUrl +
                MapTileIndex.getZoom(pMapTileIndex) + "/" +
                MapTileIndex.getX(pMapTileIndex) + "/" +
                MapTileIndex.getY(pMapTileIndex) + ".png?key=" + BuildConfig.MAPTILER_KEY
}