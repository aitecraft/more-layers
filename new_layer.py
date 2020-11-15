def make_file(filename, contents):
    f = open(filename, "w")
    f.write(contents)
    f.close()


block_name = input("Enter Block Name: ")
texture_name = "block/" + input("Enter Texture Name: block/")

stonecutting = input("Stonecutting or regular crafting? (s or type anything else) ").lower() == 's'


data_dir = "src/main/resources/data/ml"
assets_dir = "src/main/resources/assets/ml"

# First create blockstate file.

blockstate_ref = """{"variants": {"layers=1":{ "model": "ml:block/REPLACEVAR_layer_2"},"layers=2":{ "model": "ml:block/REPLACEVAR_layer_4"},"layers=3":{ "model": "ml:block/REPLACEVAR_layer_6"},"layers=4":{ "model": "ml:block/REPLACEVAR_layer_8"},"layers=5":{ "model": "ml:block/REPLACEVAR_layer_10"},"layers=6":{ "model": "ml:block/REPLACEVAR_layer_12"},"layers=7":{ "model": "ml:block/REPLACEVAR_layer_14"},"layers=8":{ "model": "ml:block/REPLACEVAR_layer_16"}}}"""

make_file(assets_dir+"/blockstates/" + block_name + "_layer.json", blockstate_ref.replace("REPLACEVAR", block_name))

# Then create item model

item_model_ref = """{"parent": "ml:block/REPLACEVAR_layer_2"}"""
make_file(assets_dir+"/models/item/"+block_name+"_layer.json", item_model_ref.replace("REPLACEVAR", block_name))

# Now create the block models

block_model_ref = """{   "parent": "ml:block/layer_LAYERNUMBER","textures": {"all": "REPLACEVAR"}}"""

for layer_num in range(2, 17, 2):
    content = (block_model_ref.replace("REPLACEVAR", texture_name)).replace("LAYERNUMBER", str(layer_num))
    make_file(assets_dir+"/models/block/"+block_name+"_layer_"+str(layer_num)+".json", content)

# Now create the loot table

loot_table_ref = """{"type": "minecraft:block","pools": [{"rolls": 1,"entries": [{"type": "minecraft:alternatives","children": [{"type": "minecraft:item","conditions": [{ "condition": "minecraft:block_state_property", "block": "ml:REPLACEVAR_layer","properties": {"layers": "1" }}],"functions": [{"function": "minecraft:set_count","count": 1}],"name": "ml:REPLACEVAR_layer"},{"type": "minecraft:item","conditions": [{ "condition": "minecraft:block_state_property", "block": "ml:REPLACEVAR_layer","properties": {"layers": "2" }}],"functions": [{"function": "minecraft:set_count","count": 2}],"name": "ml:REPLACEVAR_layer"},{"type": "minecraft:item","conditions": [{ "condition": "minecraft:block_state_property", "block": "ml:REPLACEVAR_layer","properties": {"layers": "3" }}],"functions": [{"function": "minecraft:set_count","count": 3}],"name": "ml:REPLACEVAR_layer"},{"type": "minecraft:item","conditions": [{ "condition": "minecraft:block_state_property", "block": "ml:REPLACEVAR_layer","properties": {"layers": "4" }}],"functions": [{"function": "minecraft:set_count","count": 4}],"name": "ml:REPLACEVAR_layer"},{"type": "minecraft:item","conditions": [{ "condition": "minecraft:block_state_property", "block": "ml:REPLACEVAR_layer","properties": {"layers": "5" }}],"functions": [{"function": "minecraft:set_count","count": 5}],"name": "ml:REPLACEVAR_layer"},{"type": "minecraft:item","conditions": [{ "condition": "minecraft:block_state_property", "block": "ml:REPLACEVAR_layer","properties": {"layers": "6" }}],"functions": [{"function": "minecraft:set_count","count": 6}],"name": "ml:REPLACEVAR_layer"},{"type": "minecraft:item","conditions": [{ "condition": "minecraft:block_state_property", "block": "ml:REPLACEVAR_layer","properties": {"layers": "7" }}],"functions": [{"function": "minecraft:set_count","count": 7}],"name": "ml:REPLACEVAR_layer"},{"type": "minecraft:item","conditions": [{ "condition": "minecraft:block_state_property", "block": "ml:REPLACEVAR_layer","properties": {"layers": "8" }}],"functions": [{"function": "minecraft:set_count","count": 8}],"name": "ml:REPLACEVAR_layer"}]}],"conditions": [{"condition": "minecraft:survives_explosion"}]}]}"""
make_file(data_dir+"/loot_tables/blocks/"+block_name+"_layer.json", loot_table_ref.replace("REPLACEVAR", block_name))

# Now create the recipe

crafting_ref = ""
if (stonecutting):
    crafting_ref = """{
  "type": "minecraft:stonecutting",
  "ingredient": {
    "item": "minecraft:REPLACEVAR"
  },
  "result": "ml:REPLACEVAR_layer",
  "count": 8
}"""
else:
    crafting_ref = """{"type": "minecraft:crafting_shaped","pattern": ["##"],"key": {"#": {"item": "minecraft:REPLACEVAR"}},"result": {"item": "ml:REPLACEVAR_layer","count": 16}}"""

make_file(data_dir+"/recipes/"+block_name+"_layer.json", crafting_ref.replace("REPLACEVAR", block_name))


print("You have to update the language file yourself!")
