import zipfile
import xml.etree.ElementTree as ET
import os

def parse_docx(docx_path, output_md_path):
    try:
        with zipfile.ZipFile(docx_path) as docx:
            xml_content = docx.read('word/document.xml')
            root = ET.fromstring(xml_content)
            
            # Word XML namespaces
            ns = {'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'}
            
            paragraphs = []
            for p in root.findall('.//w:p', ns):
                texts = [t.text for t in p.findall('.//w:t', ns) if t.text]
                if texts:
                    paragraphs.append(''.join(texts))
                else:
                    # Keep empty line if it's a break
                    paragraphs.append('')
            
            with open(output_md_path, 'w', encoding='utf-8') as f:
                f.write('\n'.join(paragraphs))
            print(f"Successfully parsed and wrote to {output_md_path}")
    except Exception as e:
        print("Error:", e)

if __name__ == '__main__':
    docx_file = r"c:\Users\krishna\AndroidStudioProjects\KIriCode\CodeQuest_Implementation_Guide.docx"
    output_file = r"c:\Users\krishna\AndroidStudioProjects\KIriCode\CodeQuest_Implementation_Guide.md"
    parse_docx(docx_file, output_file)
