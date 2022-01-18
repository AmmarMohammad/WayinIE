# This script was created to automate the merging and injection process of Import/Export and Wayin app.
# The script is not rigidly backed up by tries and excepts or if conditionals, it is just fragile implementation to
# make life easier.
# Ammar Mohammad on 18/01/2022

import distutils.dir_util
import fnmatch
import os.path
import shutil
import configparser

from lxml import etree


def list_elms(root, tag):
    elms = []
    for element in root.findall(tag):
        elms.append(element)
    return elms


def copy_missing(root, tag, src_elms, target_elms):
    missing = []
    for element in src_elms:
        if element not in target_elms:
            missing.append(element)

    if len(missing) > 0:
        elms = root.findall(tag)
        if len(elms) > 0:
            last_of_kind = root.index(elms[-1]) + 1
        else:
            last_of_kind = 0
        for element in missing:
            print('adding element [{0}]'.format(element.tag))
            root.insert(last_of_kind, element)


def compare_and_copy(tag, source_root, target_root):
    ie_elms = list_elms(source_root, tag)
    host_elms = list_elms(target_root, tag)

    copy_missing(target_root, tag, ie_elms, host_elms)


def search(path, name_or_pattern):
    matches = []
    for root, dir_names, filenames in os.walk(path):
        for filename in fnmatch.filter(filenames, name_or_pattern):
            matches.append(os.path.join(root, filename))
    return matches


def write(file_path, xml):
    with open(file_path, 'wb') as xml_file:
        xml_file.write(etree.tostring(xml, xml_declaration=True, encoding='utf-8', pretty_print=True))
        # xml.write(file_path, xml_declaration=True, encoding='UTF-8')


def get_last_id(kind, root):
    m = 0
    for element in root.xpath("public[@type='{0}']".format(kind)):
        m = max(m, int(element.attrib['id'], 0))
    return m


config = configparser.ConfigParser()
config.read('paths.ini')

ie_path = config['root']['ie_path']
host_path = config['root']['host_path']

if not ie_path.endswith('\\'):
    ie_path += '\\'
if not host_path.endswith('\\'):
    host_path += '\\'

print("MERGING MANIFESTS")
print("------------------")
d = etree.parse(ie_path + 'AndroidManifest.xml')
ie_root = d.getroot()

d = etree.parse(host_path + 'AndroidManifest.xml')
host_root = d.getroot()

compare_and_copy('uses-permission', ie_root, host_root)

ie_application = ie_root.find('application')
host_application = host_root.find('application')
compare_and_copy('provider', ie_application, host_application)
compare_and_copy('activity', ie_application, host_application)

write(host_path + 'AndroidManifest.xml', host_root)
print("------------------")


print("COPYING RESOURCES")
print("------------------")
ie_res_path = ie_path + 'res'
host_res_path = host_path + 'res'
source_res_decompiled_files = search(ie_res_path, '*.*')
for item in source_res_decompiled_files:
    target = host_res_path + item[len(ie_res_path):]
    if not os.path.exists(target):
        parent = os.path.dirname(target)
        if not os.path.exists(parent):
            os.makedirs(parent)
        shutil.copyfile(item, target)
        print('Copied [%s]' % target)
print("------------------")


print("MERGING RESOURCE DEFINITIONS")
print("------------------")
for folder in os.listdir(ie_res_path):
    if folder[:6] == 'values':
        # values_dir = ie_res_path + '\\values'
        values_dir = os.path.join(ie_res_path, folder)
        # target_values_dir = host_res_path + '\\values'
        target_values_dir = os.path.join(host_res_path, folder)
        definitions = os.listdir(values_dir)
        for name in definitions:
            if name == 'public.xml':
                continue
            source_path = os.path.join(values_dir, name)
            target_path = os.path.join(target_values_dir, name)
            if os.path.exists(target_path):  # And it should, given how we copied resources before
                tgt_elements = []
                tgt_definition_root = etree.parse(target_path).getroot()
                for el in tgt_definition_root:
                    tgt_elements.append(el.attrib['name'])

                src_definition_root = etree.parse(source_path).getroot()
                for el in src_definition_root:
                    el_name = el.attrib['name']
                    if el_name not in tgt_elements:
                        print('adding element {0} into {1}'.format(el_name, name))
                        tgt_definition_root.append(el)
                write(target_path, tgt_definition_root)
print("------------------")

host_public_file_path = host_res_path + "\\values\\public.xml"
ie_public_file_path = ie_res_path + "\\values\\public.xml"

d = etree.parse(host_public_file_path)
host_public_root = d.getroot()
host_public_elements = []

for el in host_public_root:
    host_public_elements.append(el.attrib['name'])

d = etree.parse(ie_public_file_path)
ie_public_root = d.getroot()
ids_map = {}


smali_files = search(ie_path + '\\smali\\ammar', '*.smali')
for el in ie_public_root:
    original_id = el.attrib['id']
    name = el.attrib['name']
    res_type = el.attrib['type']

    if name not in host_public_elements:

        if res_type in ids_map.keys():
            id = ids_map.get(res_type)
            id += 1
        else:
            id = get_last_id(res_type, host_public_root)
            if id == 0:
                id = int(el.attrib['id'], 0)
            else:
                id += 1
        ids_map.update({res_type: id})
        el.attrib['id'] = hex(id)
        print('adding element {0} with id {1}'.format(el.attrib['name'], el.attrib['id']))
        host_public_root.append(el)

        if original_id != hex(id):
            is_high16 = str(id).endswith('0000')
            search_string = ', {0}'.format(original_id)
            for file in smali_files:
                with open(file, 'r') as sf:
                    sfc = sf.read()
                    if search_string in sfc:
                        print('Found id [%s] in file [%s]' % (hex(id), file))
                        for line in sfc.split('\n'):
                            stripped = line.strip()
                            # Not fancy, but does the job
                            if stripped.startswith('const/high16') and not is_high16:
                                mod_constant = stripped.replace('const/high16', 'const')
                                mod_constant = mod_constant.replace(search_string, ', {0}'.format(hex(id)))
                                sfc = sfc.replace(stripped, mod_constant)
                                print('Changed type of constant {0}'.format(original_id))
                            else:
                                mod_constant = stripped.replace(search_string, ', {0}'.format(hex(id)))
                                sfc = sfc.replace(stripped, mod_constant)

                        with open(file, 'w') as sfm:
                            sfm.write(sfc)
write(host_public_file_path, host_public_root)


print("COPYING SOURCES")
print("------------------")
ie_code_path = ie_path + 'smali'
host_code_path = host_path + 'smali'
exclude_dirs = ['android', 'androidx', 'com']
for directory in os.listdir(ie_code_path):
    if directory in exclude_dirs:
        print('excluded ' + directory)
        continue
    src = os.path.join(ie_code_path, directory)
    dst = os.path.join(host_code_path, directory)
    distutils.dir_util.copy_tree(src, dst)
    print('copied {0}'.format(dst))
print("------------------")
print("Done!")
