/*
 * Copyright 2014-2015 See AUTHORS file.
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

package com.kotcrab.vis.editor.ui.scene.entityproperties.autotable;

import com.artemis.Component;
import com.artemis.Entity;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.editor.Editor;
import com.kotcrab.vis.editor.Icons;
import com.kotcrab.vis.editor.module.InjectModule;
import com.kotcrab.vis.editor.module.project.FileAccessModule;
import com.kotcrab.vis.editor.proxy.EntityProxy;
import com.kotcrab.vis.editor.ui.dialog.SelectFileDialog;
import com.kotcrab.vis.runtime.util.autotable.ATSelectFile;
import com.kotcrab.vis.runtime.util.autotable.ATSelectFileHandler;
import com.kotcrab.vis.ui.widget.Tooltip;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static com.kotcrab.vis.editor.util.vis.EntityUtils.getCommonString;

/** @author Kotcrab */
public class SelectFileFragmentProvider extends AutoTableFragmentProvider<ATSelectFile> {
	@InjectModule private FileAccessModule fileAccessModule;

	private ObjectMap<Field, SelectFileDialogSet> fileDialogLabels = new ObjectMap<>();

	@Override
	public void createUI (ATSelectFile annotation, Class type, Field field) {
		String fieldName = annotation.fieldName().equals("") ? field.getName() : annotation.fieldName();

		VisLabel fileLabel = new VisLabel();
		fileLabel.setColor(Color.GRAY);
		fileLabel.setEllipsis(true);

		VisImageButton selectFileButton = new VisImageButton(Icons.MORE.drawable());

		VisTable table = new VisTable(true);
		table.add(new VisLabel(fieldName));
		table.add(fileLabel).expandX().fillX();
		table.add(selectFileButton);

		Tooltip tooltip = new Tooltip(fileLabel, "");

		uiTable.add(table).expandX().fillX().row();

		ATSelectFileHandler handler = null;

		try {
			Class clazz = Class.forName(annotation.handlerClass());
			Constructor constructor = clazz.getConstructor();
			Object object = constructor.newInstance();

			if (object instanceof ATSelectFileHandler == false) {
				throw new IllegalStateException("SelectFilePropertyUI handler must be instance of SelectFilePropertyHandler");
			}

			handler = (ATSelectFileHandler) object;
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("AutoTable failed, failed to create handler with class: " + annotation.handlerClass(), e);
		}

		injector.injectModules(handler);

		fileDialogLabels.put(field, new SelectFileDialogSet(fileLabel, tooltip, handler));

		FileHandle folder = fileAccessModule.getAssetsFolder().child(annotation.relativeFolderPath());

		final ATSelectFileHandler finalHandler = handler;
		final SelectFileDialog selectFontDialog = new SelectFileDialog(annotation.extension(), annotation.hideExtension(), folder, file -> {
			for (EntityProxy proxy : properties.getProxies()) {
				for (Entity entity : proxy.getEntities()) {
					finalHandler.applyChanges(entity, file);
				}
			}

			properties.getParentTab().dirty();
			properties.selectedEntitiesChanged();
			properties.endSnapshot();
		});

		selectFileButton.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				selectFontDialog.rebuildFileList();
				properties.beginSnapshot();
				Editor.instance.getStage().addActor(selectFontDialog.fadeIn());
			}
		});
	}

	@Override
	public void updateUIFromEntities (Array<EntityProxy> proxies, Class type, Field field) throws ReflectiveOperationException {
		SelectFileDialogSet set = fileDialogLabels.get(field);
		String path = getCommonString(proxies, "<?>", set.handler::getLabelValue);
		set.fileLabel.setText(path);
		((VisLabel) set.tooltip.getContent()).setText(path);
		set.tooltip.pack();
	}

	@Override
	public void setToEntities (Class type, Field field, Component component) throws ReflectiveOperationException {

	}

	@Override
	public Object getUiByField (Class type, Field field) {
		return fileDialogLabels.get(field);
	}

	private static class SelectFileDialogSet {
		public VisLabel fileLabel;
		public Tooltip tooltip;
		public ATSelectFileHandler handler;

		public SelectFileDialogSet (VisLabel fileLabel, Tooltip tooltip, ATSelectFileHandler handler) {
			this.fileLabel = fileLabel;
			this.tooltip = tooltip;
			this.handler = handler;
		}
	}
}