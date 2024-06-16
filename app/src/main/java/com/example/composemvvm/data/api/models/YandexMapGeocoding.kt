package com.example.composemvvm.data.api.models


data class Envelope(
    val lowerCorner: String,
    val upperCorner: String
)

data class GeoObjectCollection(
    val metaDataProperty: MetaDataProperty,
    val featureMember: List<FeatureMember>
)

data class MetaDataProperty(
    val GeocoderResponseMetaData: GeocoderResponseMetaData
)

data class GeocodingResponse(
    val GeoObjectCollection: GeoObjectCollection
)

data class GeocoderResponseMetaData(
    val request: String,
    val found: String,
    val results: String
)

data class FeatureMember(
    val GeoObject: GeoObject
)

data class GeoObject(
    val metaDataProperty: MetaDataProperty,
    val description: String,
    val name: String,
    val boundedBy: BoundedBy,
    val Point: Point
)

data class BoundedBy(
    val Envelope: Envelope
)


data class Point(
    val pos: String
)

data class YandexGeocodingResponse(
    val response: GeocodingResponse
)