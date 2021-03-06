/*
 * Copyright 2014-2017 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kotcrab.vis.ui.util.value;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

/**
 * Allows to use libGDX {@link Value} with lambdas for scene2d.ui widgets. Note that this cannot be added to actors,
 * only widgets are supported, if you try to do so you will get {@link ClassCastException} when this Value has been invoked.
 * Using this on Java lower than 1.8 is pointless because lambadas are not supported.
 * @author Kotcrab
 * @see VisValue
 * @see PrefHeightIfVisibleValue
 * @since 0.9.3
 */
public class VisWidgetValue extends Value {
	protected WidgetValueGetter getter;

	public VisWidgetValue (WidgetValueGetter getter) {
		this.getter = getter;
	}

	@Override
	public float get (Actor context) {
		return getter.get((Widget) context);
	}

	public interface WidgetValueGetter {
		float get (Widget context);
	}
}
