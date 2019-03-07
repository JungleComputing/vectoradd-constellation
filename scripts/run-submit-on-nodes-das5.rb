
NODE_MAP = { 
    "TitanX" => ["node001", "node002", "node003", "node004", "node005",
	"node006", "node007", "node024", "node025", "node046", "node047",
	"node048", "node049", "node050",
	"node051", "node052"],
    "K40" => ["node027"],
    "K20" => ["node028"],
    "XeonPhi" => ["node028"],
    "TitanX-Pascal" => ["node026", "node029"],
    "regular" => ["node008", "node009",
	"node010", "node011", "node012",
	"node013", "node014", "node015",
	"node016", "node017", "node018",
	"node019", "node020", "node021",
	"node022", "node023",
	"node030", "node031", "node032",
	"node033", "node034", "node035",
	"node036", "node037", "node038",
	"node039", "node040", "node041",
	"node042", "node043", "node044",
	"node045"] }

	
$nrNodes = 0
$gpuSelection = ""

def finishGetNodes(node, selected_nodes)
  unless "#{node}" == "regular"
    $nodes += ["#{node}*#{selected_nodes.length}"]
    $gpuSelection = "#SBATCH --gres=gpu"
  end
  $nrNodes += selected_nodes.length;
  $node_names += selected_nodes
end


# EXTRA_PARAM_MAP = {
#     "K20" => "-native '-C K20 --gres=gpu:1' ",
#     "K40" => "-native '-C K40 --gres=gpu:1' ",
#     "XeonPhi" => "-native '-C XeonPhi --gres=mic:1",
#     "TitanX" => "-native '-C TitanX --gres=gpu:1' ",
#     "Titan" => "-native '-C Titan --gres=gpu:1' " }

NODES_NOT_AVAILABLE = `preserve -llist | tail -n +4 | awk 'BEGIN { FS="\\t"; x = ""} {if ($7 == "R") x = x $9 " "} END { print x }'`.split


def build_constraints
  if $nodes.length > 0
    # this form is possible, but doesn't allow a mix of nodes with different
    # amount of devices (--gres=gpu implies --gres=gpu:1).
    # we solved this by explicitly choosing the nodes
    #"-native '--gres=gpu -C [#{$nodes.join("&")}]'"
    "-native '-w #{$node_names.join(",")}'"
  else
    ""
  end
end

require("#{$bin_dir}/run-submit-on-nodes.rb")


