/*
 * Copyright 2012 Pellucid and Zenexity
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package aws.s3.models

import java.util.Date

import scala.concurrent.Future
import scala.xml.Node

import aws.s3.S3Parsers._

import aws.s3.Permissions.Grantees.{Grantee, Email}

case class LoggingStatus(
  bucket: String,
  prefix: String,
  grants: Seq[(Grantee, String)]
)

object LoggingPermisions extends Enumeration {
  type LoggingPermision = Value
  val FULL_CONTROL, READ, WRITE = Value
}

trait LoggingLayer extends HttpRequestLayer {

  object Logging {

    /**
    * Set the logging parameters for a bucket and specify permissions for who can view and modify the logging parameters.
    * To set the logging status of a bucket, you must be the bucket owner.
    * '''The logging implementation a beta feature of S3.'''
    * @param loggedBucket The name of the bucket you want to enable Logging on.
    * @param targetBucket The name of the bucket where Logs will be stored.
    * @param grantees Seq of Grantee allowed to access Logs
    */
    def enable(
      loggedBucket: String,
      targetBucket: String,
      grantees:     Seq[(Email, LoggingPermisions.LoggingPermision)] = Nil
    ) = {
      val b =
        <BucketLoggingStatus xmlns="http://doc.s3.amazonaws.com/2006-03-01">
          <LoggingEnabled>
            <TargetBucket>{ targetBucket.toLowerCase }</TargetBucket>
            <TargetPrefix>{ loggedBucket.toLowerCase }-access_log-/</TargetPrefix>
            <TargetGrants>
              { for (g <- grantees) yield
              <Grant>
                <Grantee xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="AmazonCustomerByEmail">
                  <EmailAddress>{ g._1.value }</EmailAddress>
                </Grantee>
                <Permission>{ g._2.toString }</Permission>
              </Grant>
            }
            </TargetGrants>
          </LoggingEnabled>
        </BucketLoggingStatus>

      Http.put[Node, Unit](
        Some(loggedBucket),
        body = b,
        subresource = Some("logging")
      )
    }

    /**
    * return the logging status of a bucket and the permissions users have to view and modify that status. To use {{{get}}}, you must be the bucket owner.
    * @param bucketName The name of the bucket.
    */
    def get(bucketName: String) =
      Http.get[Seq[LoggingStatus]](
        Some(bucketName),
        subresource = Some("logging")
      )

  }

}
