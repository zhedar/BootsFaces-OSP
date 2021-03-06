/**
 *  Copyright 2014-15 by Riccardo Massera (TheCoder4.Eu), Stephan Rauh (http://www.beyondjava.net) and Dario D'Urzo.
 *
 *  This file is part of BootsFaces.
 *
 *  BootsFaces is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  BootsFaces is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with BootsFaces. If not, see <http://www.gnu.org/licenses/>.
 */
package net.bootsfaces.component.accordion;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;

import net.bootsfaces.component.panel.Panel;
import net.bootsfaces.render.CoreRenderer;
import net.bootsfaces.render.Responsive;
import net.bootsfaces.render.Tooltip;

/** This class generates the HTML code of &lt;b:accordion /&gt;. */
@FacesRenderer(componentFamily = "net.bootsfaces.component", rendererType = "net.bootsfaces.component.accordion.Accordion")
public class AccordionRenderer extends CoreRenderer {

	/**
	 * Override encodeChildren because the children rendering is driven by the
	 * custom encode of accordion component
	 */
	@Override
	public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
		// Children are already rendered in encodeBegin()
	}

	/**
	 * Force true to prevent JSF child rendering
	 */
	@Override
	public boolean getRendersChildren() {
		return true;
	}

	/**
	 * This methods generates the HTML code of the current b:accordion.
	 * <code>encodeBegin</code> generates the start of the component. After the,
	 * the JSF framework calls <code>encodeChildren()</code> to generate the
	 * HTML code between the beginning and the end of the component. For
	 * instance, in the case of a panel component the content of the panel is
	 * generated by <code>encodeChildren()</code>. After that,
	 * <code>encodeEnd()</code> is called to generate the rest of the HTML code.
	 *
	 * @param context
	 *            the FacesContext.
	 * @param component
	 *            the current b:accordion.
	 * @throws IOException
	 *             thrown if something goes wrong when writing the HTML code.
	 */
	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		if (!component.isRendered()) {
			return;
		}
		Accordion accordion = (Accordion) component;
		ResponseWriter rw = context.getResponseWriter();
		String clientId = accordion.getClientId();
		String accordionClientId = clientId.replace(":", "_");

		List<String> expandedIds = (null != accordion.getExpandedPanels())
				? Arrays.asList(accordion.getExpandedPanels().split(",")) : null;

		rw.startElement("div", accordion);
		rw.writeAttribute("class", "panel-group" + Responsive.getResponsiveStyleClass(accordion), null);
		rw.writeAttribute("id", accordionClientId, "id");
		Tooltip.generateTooltip(context, component, rw);

		if (accordion.getChildren() != null && accordion.getChildren().size() > 0) {
			for (UIComponent _child : accordion.getChildren()) {
				if (_child instanceof Panel && ((Panel) _child).isCollapsible()) {
					Panel _childPane = (Panel) _child;
					_childPane.setAccordionParent(accordionClientId);
					if (null != expandedIds && expandedIds.contains(_childPane.getClientId()))
						_childPane.setCollapsed(false);
					else
						_childPane.setCollapsed(true);
					_childPane.encodeAll(context);
				} else {
					throw new FacesException("Accordion must contains only collapsible panel components", null);
				}
			}
		}
	}

	/**
	 * This methods generates the HTML code of the current b:accordion.
	 * <code>encodeBegin</code> generates the start of the component. After the,
	 * the JSF framework calls <code>encodeChildren()</code> to generate the
	 * HTML code between the beginning and the end of the component. For
	 * instance, in the case of a panel component the content of the panel is
	 * generated by <code>encodeChildren()</code>. After that,
	 * <code>encodeEnd()</code> is called to generate the rest of the HTML code.
	 *
	 * @param context
	 *            the FacesContext.
	 * @param component
	 *            the current b:accordion.
	 * @throws IOException
	 *             thrown if something goes wrong when writing the HTML code.
	 */
	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		if (!component.isRendered()) {
			return;
		}
		ResponseWriter rw = context.getResponseWriter();
		rw.endElement("div");
		Tooltip.activateTooltips(context, component);
	}
}
