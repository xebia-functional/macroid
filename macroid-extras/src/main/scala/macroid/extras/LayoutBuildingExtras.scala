/*
 * Copyright (C) 2015 47 Degrees, LLC http://47deg.com hello@47deg.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package macroid.extras

import android.view.{ LayoutInflater, ViewGroup }
import macroid.ContextWrapper

object LayoutBuildingExtra {

  def connect[W](id: Int)(implicit root: RootView): Option[W] = Some(root.view.findViewById(id).asInstanceOf[W])

}

class RootView(layout: Int)(implicit context: ContextWrapper) {
  val view = LayoutInflater.from(context.application).inflate(layout, null).asInstanceOf[ViewGroup]
}
