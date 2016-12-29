package br.ufu.facom.ppgco.ia.gephi.plugin.layout;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = LayoutBuilder.class)
public class GridLayoutBuilder implements LayoutBuilder {

    @Override
    public String getName() {
        return NbBundle.getMessage(GridLayoutBuilder.class, "GridLayoutBuilder.name");
    }

    @Override
    public LayoutUI getUI() {

        return new LayoutUI() {

            @Override
            public String getDescription() {
                return NbBundle.getMessage(GridLayoutBuilder.class, "GridLayoutBuilder.desc");
            }

            @Override
            public Icon getIcon() {
                return null;
            }

            @Override
            public JPanel getSimplePanel(Layout layout) {
                return null;
            }

            @Override
            public int getQualityRank() {
                return -1;
            }

            @Override
            public int getSpeedRank() {
                return -1;
            }
        };
    }

    @Override
    public Layout buildLayout() {
        return new GridLayout(this);
    }
}
