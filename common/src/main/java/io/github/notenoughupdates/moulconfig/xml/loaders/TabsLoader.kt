package io.github.notenoughupdates.moulconfig.xml.loaders

import io.github.notenoughupdates.moulconfig.gui.component.TabComponent
import io.github.notenoughupdates.moulconfig.observer.GetSetter
import io.github.notenoughupdates.moulconfig.xml.XMLContext
import io.github.notenoughupdates.moulconfig.xml.XMLGuiLoader
import io.github.notenoughupdates.moulconfig.xml.XMLUniverse
import io.github.notenoughupdates.moulconfig.xml.XSDGenerator
import org.w3c.dom.Element
import javax.xml.namespace.QName

class TabsLoader : XMLGuiLoader<TabComponent> {
    override fun createInstance(context: XMLContext<*>, element: Element): TabComponent {
        val tabElements = element.getElementsByTagName("Tab")
        val tabs = mutableListOf<TabComponent.Tab>()
        for (i in 0 until tabElements.length) {
            val tabElement = tabElements.item(i) as Element
            val body = tabElement.getElementsByTagName("Tab.Body").item(0) as Element
            val header = tabElement.getElementsByTagName("Tab.Header").item(0) as Element
            tabs.add(
                TabComponent.Tab(
                    context.getChildFragment(header),
                    context.getChildFragment(body),
                )
            )
        }
        return TabComponent(
            tabs,
            context.getPropertyFromAttribute(element, QName("selectedTabIndex"), Int::class.java)
                ?: GetSetter.floating(context.getPropertyFromAttribute(element, QName("initialSelectedTabIndex"), Int::class.java, 0))
        )
    }

    override fun getName(): QName {
        return XMLUniverse.qName("Tabs")
    }

    override fun emitXSDType(generator: XSDGenerator, root: Element): Element {
        val typeNode = generator.createChild(root, generator.xmlSchemaNamespace, "complexType")
        typeNode.setAttribute("name", name.localPart)
        val complexContent = generator.createChild(typeNode, generator.xmlSchemaNamespace, "complexContent")
        val extension = generator.createChild(complexContent, generator.xmlSchemaNamespace, "extension")
        extension.setAttribute("base", "Tabs.Content")
        val attributeTab = generator.createChild(extension, generator.xmlSchemaNamespace, "attribute")
        attributeTab.setAttribute("name", "selectedTabIndex")
        val attributeDefaultTab = generator.createChild(extension, generator.xmlSchemaNamespace, "attribute")
        attributeDefaultTab.setAttribute("name", "initialSelectedTabIndex")

        val childTypeNode = generator.createChild(root, generator.xmlSchemaNamespace, "complexType")
        childTypeNode.setAttribute("name", "Tabs.Content")
        val sequence = generator.createChild(childTypeNode, generator.xmlSchemaNamespace, "sequence")
        sequence.setAttribute("maxOccurs", "unbounded")
        val sequenceElement = generator.createChild(sequence, generator.xmlSchemaNamespace, "element")
        sequenceElement.setAttribute("name", "Tab")

        val tabType = generator.createChild(sequenceElement, generator.xmlSchemaNamespace, "complexType")
        val tabSequence = generator.createChild(tabType, generator.xmlSchemaNamespace, "sequence")
        val tabHeader = generator.createChild(tabSequence, generator.xmlSchemaNamespace, "element")
        tabHeader.setAttribute("name", "Tab.Header")
        tabHeader.setAttribute("type", "SingleWidget")
        val tabBody = generator.createChild(tabSequence, generator.xmlSchemaNamespace, "element")
        tabBody.setAttribute("name", "Tab.Body")
        tabBody.setAttribute("type", "SingleWidget")
        return typeNode
    }
}
