/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2011 Pentaho Corporation..  All rights reserved.
 * 
 * @author Ezequiel Cuellar
 */

package org.pentaho.platform.dataaccess.datasource.wizard;

import java.util.List;

import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.models.JoinFieldModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.JoinGuiModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.JoinTableModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.JoinSelectionServiceGwtImpl;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractModelList;

@SuppressWarnings("unchecked")
public class JoinDefinitionStepController extends AbstractXulEventHandler {

	protected static final String JOIN_DEFINITION_PANEL_ID = "joinDefinitionWindow";

	private XulDialog joinDefinitionDialog;
	private JoinGuiModel joinGuiModel;
	private XulListbox leftTables;
	private XulListbox rightTables;
	private XulMenuList<JoinFieldModel> leftKeyFieldList;
	private XulMenuList<JoinFieldModel> rightKeyFieldList;
	private JoinSelectionServiceGwtImpl joinSelectionServiceGwtImpl;
	private IConnection selectedConnection;

	public JoinDefinitionStepController(JoinGuiModel joinGuiModel, JoinSelectionServiceGwtImpl joinSelectionServiceGwtImpl, IConnection selectedConnection) {
		this.joinGuiModel = joinGuiModel;
		this.joinSelectionServiceGwtImpl = joinSelectionServiceGwtImpl;
		this.selectedConnection = selectedConnection;
	}

	public void init() {

		XulDomContainer mainContainer = getXulDomContainer();
		Document rootDocument = mainContainer.getDocumentRoot();
		mainContainer.addEventHandler(this);
		this.joinDefinitionDialog = (XulDialog) rootDocument.getElementById(JOIN_DEFINITION_PANEL_ID);
		this.leftTables = (XulListbox) rootDocument.getElementById("leftTables");
		this.rightTables = (XulListbox) rootDocument.getElementById("rightTables");
		this.leftKeyFieldList = (XulMenuList<JoinFieldModel>) rootDocument.getElementById("leftKeyField");
		this.rightKeyFieldList = (XulMenuList<JoinFieldModel>) rootDocument.getElementById("rightKeyField");

		BindingFactory bf = new GwtBindingFactory(rootDocument);

		Binding leftTablesBinding = bf.createBinding(this.joinGuiModel.getSelectedTables(), "children", this.leftTables, "elements");
		Binding rightTablesBinding = bf.createBinding(this.joinGuiModel.getSelectedTables(), "children", this.rightTables, "elements");

		Binding leftTableSelectionBinding = bf.createBinding(this.leftTables, "selectedItem", this.joinGuiModel, "leftJoinTable");
		Binding rightTableSelectionBinding = bf.createBinding(this.rightTables, "selectedItem", this.joinGuiModel, "rightJoinTable");

		Binding leftTableFieldsBinding = bf.createBinding(this.joinGuiModel.getLeftJoinTable().getFields(), "children", this.leftKeyFieldList, "elements");
		Binding rightTableFieldsBinding = bf.createBinding(this.joinGuiModel.getRightJoinTable().getFields(), "children", this.rightKeyFieldList, "elements");

		Binding leftTableSelectedItemBinding = bf.createBinding(this.leftTables, "selectedItem", this.joinGuiModel.getLeftJoinTable(), "fields", new TableFieldsConvertor());
		Binding rightTableSelectedItemBinding = bf.createBinding(this.rightTables, "selectedItem", this.joinGuiModel.getRightJoinTable(), "fields", new TableFieldsConvertor());

		try {
			leftTablesBinding.fireSourceChanged();
			rightTablesBinding.fireSourceChanged();
			leftTableSelectionBinding.fireSourceChanged();
			rightTableSelectionBinding.fireSourceChanged();
			leftTableFieldsBinding.fireSourceChanged();
			rightTableFieldsBinding.fireSourceChanged();
			leftTableSelectedItemBinding.fireSourceChanged();
			rightTableSelectedItemBinding.fireSourceChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class TableFieldsConvertor extends BindingConvertor<Object, AbstractModelList<JoinFieldModel>> {

		private AbstractModelList<JoinFieldModel> tableFields = null;

		@Override
		public AbstractModelList<JoinFieldModel> sourceToTarget(final Object object) {

			final JoinTableModel table = (JoinTableModel) object;

			joinSelectionServiceGwtImpl.getTableFields(table.getName(), selectedConnection, new XulServiceCallback<List>() {
				public void error(String message, Throwable error) {
					error.printStackTrace();
				}

				public void success(List fields) {
					List<JoinFieldModel> fieldModels = table.processTableFields(fields);
					tableFields = new AbstractModelList<JoinFieldModel>(fieldModels);
				}
			});
			return tableFields;
		}

		@Override
		public Object targetToSource(final AbstractModelList<JoinFieldModel> value) {
			return null;
		}
	}

	public String getName() {
		return "joinDefinitionStepController";
	}

	@Bindable
	public void next() {
		this.joinDefinitionDialog.show();
	}

}
