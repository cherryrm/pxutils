//
// Copyright 2014-2018 Paytronix Systems, Inc.
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

package com.paytronix.utils.interchange.format.json

import org.joda.time.{
    DateTime          => JodaDateTime,
    DateTimeZone      => JodaDateTimeZone,
    LocalDate         => JodaLocalDate,
    LocalDateTime     => JodaLocalDateTime,
    LocalTime         => JodaLocalTime
}

import com.paytronix.utils.interchange.base.datetime

object joda extends joda

trait joda {
    object jodaAsIso8601 {
        implicit val dateTimeJsonCoder      : JsonCoder[JodaDateTime]      = scalar.stringJsonCoder.mapBijection(datetime.joda.iso8601.dateTimeBijection)
        implicit val localDateJsonCoder     : JsonCoder[JodaLocalDate]     = scalar.stringJsonCoder.mapBijection(datetime.joda.iso8601.localDateBijection)
        implicit val localDateTimeJsonCoder : JsonCoder[JodaLocalDateTime] = scalar.stringJsonCoder.mapBijection(datetime.joda.iso8601.localDateTimeBijection)
        implicit val localTimeJsonCoder     : JsonCoder[JodaLocalTime]     = scalar.stringJsonCoder.mapBijection(datetime.joda.iso8601.localTimeBijection)
    }

    object jodaAsClassic {
        implicit val dateTimeJsonCoder      : JsonCoder[JodaDateTime]      = scalar.stringJsonCoder.mapBijection(datetime.joda.classic.dateTimeBijection)
        implicit val localDateJsonCoder     : JsonCoder[JodaLocalDate]     = scalar.stringJsonCoder.mapBijection(datetime.joda.classic.localDateBijection)
        implicit val localDateTimeJsonCoder : JsonCoder[JodaLocalDateTime] = scalar.stringJsonCoder.mapBijection(datetime.joda.classic.localDateTimeBijection)
        implicit val localTimeJsonCoder     : JsonCoder[JodaLocalTime]     = scalar.stringJsonCoder.mapBijection(datetime.joda.classic.localTimeBijection)
    }
}
