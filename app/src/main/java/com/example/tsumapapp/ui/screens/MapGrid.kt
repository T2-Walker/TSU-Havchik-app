package com.example.tsumapapp.ui.screens

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.util.GeoPoint

class GridOverlay : Overlay() { //наследуем GridOverlay от оверлея osmd
    private val paint = Paint().apply{//Настраиваем нашу кисточку для отрисовки
        color = Color.LTGRAY
        strokeWidth = 1.5f //Толщина линий
        style = Paint.Style.STROKE//без заливки
    }
    private val cellSizeDegrees = 0.000028  // размер по широте в градусах (lat)
    private val cellSizeLng = cellSizeDegrees / Math.cos(Math.toRadians(56.47))//для квадратиков делим на коофицент широты(примерно)
    //переопределяем метод, "когда OSMDroid захочет нарисовать этот overlay — делай вот это"

    override fun draw (canvas: Canvas, mapView: MapView, shadow: Boolean){
        if(shadow) return//шобы сетка не рисовалась 2 раза из-за теней

        val bbox = mapView.boundingBox//видимый прямоугльный части краты када двигаем ее он меняется
        val north = bbox.latNorth//широты
        val south = bbox.latSouth
        val west = bbox.lonWest
        val east = bbox.lonEast

        val projection = mapView.projection//переводчик между координатами и пикселями экрана

        val startLat = Math.floor(south / cellSizeDegrees) * cellSizeDegrees//шобы не "плыли" при движении карты и начанались ВСЕГДА с 1 точек
        val startLng = Math.floor(west  / cellSizeLng) * cellSizeLng

        // рисуем вертикальные линии (по долготе)
        var lng = startLng //начинаем с левого края т.к будем менять занчение в цикле
        while (lng <= east) {// идём вправо пока не дойдём до правого края экрана.
            val top = projection.toPixels(GeoPoint(north, lng), null)// объект с полями .x и .y
            val bottom = projection.toPixels(GeoPoint(south, lng), null)
            canvas.drawLine(//рисуем линию от верхней точки до нижней точки с нашей кисточкой
                top.x.toFloat(), top.y.toFloat(),
                bottom.x.toFloat(), bottom.y.toFloat(),
                paint
            )
            lng += cellSizeLng  //шагаем вПРАВО на 1 клетку
        }
        //теперь горизонатльные линии тоже самое
        var lat = startLat
        while (lat <= north) {
            val left  = projection.toPixels(GeoPoint(lat, west), null)
            val right = projection.toPixels(GeoPoint(lat, east), null)
            canvas.drawLine(
                left.x.toFloat(), left.y.toFloat(),
                right.x.toFloat(), right.y.toFloat(),
                paint
            )
            lat += cellSizeDegrees
        }
    }
}