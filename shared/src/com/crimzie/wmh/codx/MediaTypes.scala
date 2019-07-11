package com.crimzie.wmh
package codx

import tapir.{Codec, MediaType}

object MediaTypes {

  case class ImagePng() extends MediaType {
    override val mediaTypeNoParams = "image/png"
  }

  case class ImageJpg() extends MediaType {
    override val mediaTypeNoParams = "image/jpg"
  }

  case class TextCss() extends MediaType {
    override val mediaTypeNoParams = "text/css"
  }

  implicit val imagePngCodec: Codec[Array[Byte], ImagePng, _] =
    Codec.byteArrayCodec.mediaType(ImagePng())

  implicit val imageJpgCodec: Codec[Array[Byte], ImageJpg, _] =
    Codec.byteArrayCodec.mediaType(ImageJpg())

  implicit val textCssCodec: Codec[String, TextCss, _] =
    Codec.stringPlainCodecUtf8.mediaType(TextCss())

}
