package org.jboss.as.console.client.tools;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import org.jboss.as.console.client.Console;
import org.jboss.as.console.client.shared.viewframework.builder.MultipleToOneLayout;
import org.jboss.as.console.client.widgets.pages.PagedView;
import org.jboss.as.console.client.widgets.tables.TextLinkCell;
import org.jboss.ballroom.client.widgets.forms.Form;
import org.jboss.ballroom.client.widgets.forms.TextBoxItem;
import org.jboss.ballroom.client.widgets.forms.TextItem;
import org.jboss.ballroom.client.widgets.tables.DefaultCellTable;
import org.jboss.ballroom.client.widgets.tools.ToolButton;
import org.jboss.ballroom.client.widgets.tools.ToolStrip;

import java.util.Set;

/**
 * @author Heiko Braun
 * @date 7/23/12
 */
public class StorageView {

    private StoragePresenter presenter;
    private DefaultCellTable<FXTemplate> table;
    private ListDataProvider<FXTemplate> dataProvider;
    private PagedView pages;
    private TemplateModelsView modelStepView;

    public StorageView() {

    }

    public void setPresenter(StoragePresenter presenter) {
        this.presenter = presenter;
        modelStepView.setPresenter(presenter);
    }

    Widget asWidget() {

        pages = new PagedView();

        table = new DefaultCellTable<FXTemplate>(8,
                new ProvidesKey<FXTemplate>() {
                    @Override
                    public Object getKey(FXTemplate fxTemplate) {
                        return fxTemplate.getId();
                    }
                });

        dataProvider = new ListDataProvider<FXTemplate>();
        dataProvider.addDataDisplay(table);

        final SingleSelectionModel<FXTemplate> selectionModel = new SingleSelectionModel<FXTemplate>();
        table.setSelectionModel(selectionModel);

        TextColumn<FXTemplate> idCol = new TextColumn<FXTemplate>() {
            @Override
            public String getValue(FXTemplate fxTemplate) {
                return fxTemplate.getId();
            }

        };

        TextColumn<FXTemplate> nameCol = new TextColumn<FXTemplate>() {
            @Override
            public String getValue(FXTemplate fxTemplate) {
                return fxTemplate.getName();
            }
        };

        table.addColumn(idCol, "ID");
        table.addColumn(nameCol, "Name");

        Column<FXTemplate, FXTemplate> option = new Column<FXTemplate, FXTemplate>(
                new TextLinkCell<FXTemplate>(Console.CONSTANTS.common_label_view(),
                        new ActionCell.Delegate<FXTemplate>() {
                    @Override
                    public void execute(FXTemplate selection) {
                        pages.showPage(1);
                    }
                })
        ) {

            @Override
            public FXTemplate getValue(FXTemplate t) {
                return t;
            }
        };

        table.addColumn(option, "Option");

        ToolStrip toolstrip = new ToolStrip();
        ToolButton addBtn = new ToolButton("Add", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                presenter.launchNewTemplateWizard();
            }
        });

        ToolButton removeBtn = new ToolButton("Remove", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                final FXTemplate selectedObject = selectionModel.getSelectedObject();
                presenter.onRemoveTemplate(selectedObject.getId());

            }
        });

        toolstrip.addToolButtonRight(addBtn);
        toolstrip.addToolButtonRight(removeBtn);


        final Form<Object> form = new Form(Object.class);
        final TextItem id = new TextItem("id", "ID");
        final TextBoxItem name = new TextBoxItem("name", "Name", true);
        form.setFields(id, name);

        modelStepView = new TemplateModelsView();

        table.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                form.clearValues();
                final FXTemplate template = selectionModel.getSelectedObject();

                if(template!=null)
                {
                    id.setValue(template.getId());
                    name.setValue(template.getName());

                    // update the model step views
                    modelStepView.setModelSteps(template, template.getModels());
                }
            }
        });

        VerticalPanel formLayout = new VerticalPanel();
        formLayout.add(form.asWidget());

        MultipleToOneLayout layout = new MultipleToOneLayout()
                .setHeadline("Templates")
                .setPlain(true)
                .setDescription("UI Templates for generic model updates.")
                .setMaster("Available Templates", table)
                .setTopLevelTools(toolstrip)
                .setDetail("Detail", formLayout);


        pages.addPage(Console.CONSTANTS.common_label_back(), layout.build());
        pages.addPage("Models", modelStepView.asWidget());

        pages.showPage(0);

        return pages.asWidget();
    }

    public void setTemplates(Set<FXTemplate> fxTemplates) {
        dataProvider.getList().clear();
        dataProvider.getList().addAll(fxTemplates);
    }
}