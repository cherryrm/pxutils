//
// Copyright 2014 Paytronix Systems, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.paytronix.utils.interchange.format.avro

import scala.annotation.{Annotation, StaticAnnotation}
import scala.language.experimental.macros

import com.paytronix.utils.interchange.base

/**
 * Annotations and macros to derive coders automatically at compile time.
 *
 * Intended to be used qualified, e.g. @derive.structure.implicitCoder.
 */
object derive {
    object structure {
        /**
         * Automatically generate an `AvroCoder` for a structural type.
         *
         * For example:
         *
         *     @derive.structure.implicitCoder
         *     final case class MyStruct(a: Int, b: String)
         *
         * Will put an implicit coder on the companion object of `MyStruct` creating that companion object if it doesn't exist already,
         * like:
         *
         *     object MyStruct {
         *         implicit val avroCoder = derive.structure.coder[MyStruct]
         *     }
         *     final case class MyStruct(a: Int, b: String)
         */
        /* 2014-08-27 RMM: having multiple annotation macros which addToCompanion causes the compiler to not emit the object class (Blah$) even though
                           it doesn't error at runtime.
        class implicitCoder extends StaticAnnotation {
            def macroTransform(annottees: Any*): Any = macro deriveImpl.deriveImplicitStructureCoderAnnotation
        }
        */

        /**
         * Make the annotated class or object a complete `AvroCoder` for the given type allowing for individual field codings to be altered.
         *
         * For example:
         *
         *     final case class MyStruct(a: BigDecimal, b: String)
         *     object MyStruct {
         *         @derive.structure.customizedCoder[MyStruct]
         *         implicit object avroCoder {
         *             val aCoder = scalaBigDecimalAvroCoderString
         *         }
         *     }
         *
         * will create a coder identical to the one generated by `derive.structure.coder` or `@derive.structure.implicitCoder` but with the
         * coder used for the field `a` overridden from the default.
         */
        class customizedCoder[A] extends StaticAnnotation {
            def macroTransform(annottees: Any*): Any = macro deriveImpl.structureCoderAnnotation
        }

        /**
         * Make the annotated class or object a complete `AvroEncoder` for the given type allowing for individual field encodings to be altered.
         * See `customizedCoder` for an example.
         */
        class customizedEncoder[A] extends StaticAnnotation {
            def macroTransform(annottees: Any*): Any = macro deriveImpl.structureEncoderAnnotation
        }

        /**
         * Make the annotated class or object a complete `AvroDecoder` for the given type allowing for individual field decodings to be altered.
         * See `customizedCoder` for an example.
         */
        class customizedDecoder[A] extends StaticAnnotation {
            def macroTransform(annottees: Any*): Any = macro deriveImpl.structureDecoderAnnotation
        }

        /**
         * Generate an `AvroCoder` for the given type, using coders from the implicit scope for field codings.
         * Fails at compile time if a coding can't be determined for a field.
         */
        def coder[A]: AvroCoder[A] = macro deriveImpl.structureCoderDef[A]

        /**
         * Generate an `AvroEncoder` for the given type, using encoders from the implicit scope for field encodings.
         * Fails at compile time if a encoding can't be determined for a field.
         */
        def encoder[A]: AvroEncoder[A] = macro deriveImpl.structureEncoderDef[A]

        /**
         * Generate an `AvroDecoder` for the given type, using decoders from the implicit scope for field decodings.
         * Fails at compile time if a decoding can't be determined for a field.
         */
        def decoder[A]: AvroDecoder[A] = macro deriveImpl.structureDecoderDef[A]
    }

    /**
     * Annotation and macros to derive coding for wrapper types - types with a single field which wrap some other type and are represented when
     * encoded as that other type. Sometimes this is called a newtype, sometimes a value type, but for the purposes of encoding and decoding any
     * class with a single field counts.
     *
     * For example:
     *
     *     final case class Meters(m: Int) extends AnyVal
     *     object Meters {
     *         implicit val avroCoder: AvroCoder[Meters] = derive.wrapper.coder[Meters]
     *     }
     *
     * In this example, a value of type `Meters` would be encoded as an integer, but is a distinct type in Scala.
     */
    object wrapper {
        /* 2014-08-27 RMM: having multiple annotation macros which addToCompanion causes the compiler to not emit the object class (Blah$) even though
                           it doesn't error at runtime.
        class implicitCoder extends StaticAnnotation {
            def macroTransform(annottees: Any*): Any = macro deriveImpl.deriveImplicitWrapperCoderAnnotation
        }
        */

        /**
         * Generate an `AvroCoder` for the given wrapping type, using a coder from the implicit scope for the single field decoding.
         * Fails at compile time if a decoding can't be determined.
         */
        def coder[A]: AvroCoder[A] = macro deriveImpl.wrapperCoderDef[A]

        /**
         * Generate an `AvroEncoder` for the given wrapping type, using an encoder from the implicit scope for the single field encoding.
         * Fails at compile time if a decoding can't be determined.
         */
        def encoder[A]: AvroEncoder[A] = macro deriveImpl.wrapperEncoderDef[A]

        /**
         * Generate an `AvroDecoder` for the given wrapping type, using a decoder from the implicit scope for the single field decoding.
         * Fails at compile time if a decoding can't be determined.
         */
        def decoder[A]: AvroDecoder[A] = macro deriveImpl.wrapperDecoderDef[A]
    }

    /** Annotation and macro to derive union (sum type) coders. */
    object union {
        /**
         * Automatically generate an `AvroCoder` for a union type whose alternates have been enumerated with
         * a `com.paytronix.utils.interchange.base.union` annotation.
         *
         * For example:
         *
         *     @derive.union.implicitCoder
         *     @union(union.alt[First], union.alt[Second])
         *     sealed abstract class MyUnion
         *     final case class First extends MyUnion
         *     final case class Second extends MyUnion
         *
         * Will put an implicit coder on the companion object of `MyUnion` creating that companion object if it doesn't exist already,
         * like:
         *
         *     object MyUnion {
         *         implicit val avroCoder = derive.union.coder(union.alt[First], union.alt[Second])
         *     }
         *     sealed abstract class MyUnion
         *     final case class First extends MyUnion
         *     final case class Second extends MyUnion
         */
        /* 2014-08-27 RMM: having multiple annotation macros which addToCompanion causes the compiler to not emit the object class (Blah$) even though
                           it doesn't error at runtime.
        class implicitCoder extends StaticAnnotation {
            def macroTransform(annottees: Any*): Any = macro deriveImpl.deriveImplicitUnionCoderAnnotation
        }
        */

        private[union] sealed trait Alternate[A]

        /** Declare a single alternate of a union. Intended only for use as syntax in a @derive.union.* annotation */
        def alternate[A]: Alternate[A] = new Alternate[A] { }

        /**
         * Derive an `AvroCoder` for a union (sum type), given explicitly named subtypes as alternates. The alternates may be tagged,
         * but the tag will be ignored as Avro uses a discriminator to choose among union alternates.
         */
        def coder[A](alternates: Alternate[_ <: A]*): AvroCoder[A] = macro deriveImpl.unionCoderDef[A]

        /**
         * Derive an `AvroEncoder` for a union (sum type), given explicitly named subtypes as alternates. The alternates may be tagged,
         * but the tag will be ignored as Avro uses a discriminator to choose among union alternates.
         */
        def encoder[A](alternates: Alternate[_ <: A]*): AvroEncoder[A] = macro deriveImpl.unionEncoderDef[A]

        /**
         * Derive an `AvroDecoder` for a union (sum type), given explicitly named subtypes as alternates. The alternates may be tagged,
         * but the tag will be ignored as Avro uses a discriminator to choose among union alternates.
         */
        def decoder[A](alternates: Alternate[_ <: A]*): AvroDecoder[A] = macro deriveImpl.unionDecoderDef[A]
    }
}
