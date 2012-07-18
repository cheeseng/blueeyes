package blueeyes.core.service.engines.security

import org.apache.commons.codec.binary.Base64
import org.scalatest._
import EncoderUtil._

class CertificateDecoderSpec extends WordSpec with MustMatchers with CertificateData{

  "CertificateKeyEntry must create key and certificate" in {
    val entry = CertificateDecoder(encodedPrivateKey, encodedCertificate)

    encode(entry._1.getEncoded) must equal (encodedPrivateKey)
    encode(entry._2.getEncoded) must equal (encodedCertificate)
  }

  private def encode(content: Array[Byte]) = unify(Base64.encodeBase64String(content))
}

class CertificateEncoderSpec extends WordSpec with MustMatchers with CertificateData{

  "CertificateKeyEntry must create key and certificate" in {
    val entry   = CertificateDecoder(encodedPrivateKey, encodedCertificate)
    val encoded = CertificateEncoder(entry._1, entry._2)

    unify(encoded._1) must equal (encodedPrivateKey)
    unify(encoded._2) must equal (encodedCertificate)
  }
}

object EncoderUtil{
  def unify(value: String) = value.replaceAll("\r\n", "\n").trim
}
