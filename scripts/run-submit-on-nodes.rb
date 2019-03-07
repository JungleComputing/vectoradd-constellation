
$debug = true


if ARGV.length == 0
    puts "need a node specification"
    exit 1
end

$nodes = []
$node_names = []
$extra_params = []

class String
    def is_i?
	!!(self =~ /\A[-+]?[0-9]+\z/)
    end
end


def check_node(node)
    if not NODE_MAP.keys.include? node
	print node, " not in ", NODE_MAP.keys.join(", "), "\n"
	exit 1
    end
end

def get_nodes(node, numberString) 
    check_node(node)

    available_nodes = NODE_MAP[node] - NODES_NOT_AVAILABLE

    if numberString == "all"
	number = NODE_MAP[node].length
    elsif numberString.is_i?
	number = numberString.to_i
    else
	print "unrecognized number for #{node}"
	exit 1
    end

    selected_nodes = available_nodes[0, number]
    if selected_nodes.length < number
	print "can only find ", selected_nodes.length, " ", node, " nodes\n"
	if $debug
	    print "available nodes: ", available_nodes.join(", "), "\n"
	    print "selected_nodes: ", selected_nodes.join(", "), "\n"
	end
        answer = ask
	if answer == "stop"
	  exit 0
        elsif answer == "wait"
          selected_nodes = NODE_MAP[node][0, number];
	end
    end
    print "selecting ", selected_nodes.length, " ", node, 
	  " node#{selected_nodes.length == 1 ? "" : "s"}\n"
    finishGetNodes(node, selected_nodes)
end


def isNodeSpec(arg)
    arg_spec = arg.split("=")
    arg_spec.length == 2 && NODE_MAP.keys.include?(arg_spec[0])
end

$node_specs, other_args = ARGV.partition { |a| isNodeSpec a }

if other_args.length < 3
    puts "not enough arguments"
    exit 1
end

if $node_specs.length == 0
    puts "need a node specification: <node=1> or <node=all>"
    puts "node types: #{NODE_MAP.keys.join(", ")}"
    exit 1
end

$node_specs.each do |a|
    arg_spec = a.split("=")
    if arg_spec.length == 2
	node = arg_spec[0]
	number = arg_spec[1]
	get_nodes(node, number)
    else
	puts "wrong arguments"
	exit 1
    end
end



basedir = other_args[0]
jar = other_args[1]
$classname = other_args[2]
$configargs = other_args[3].split(",").join(" ")
if other_args.length > 4 && other_args[4].split("=")[0] == "--time"
  $time = other_args[4].split("=")[1]
  $rest = other_args[5..-1]
else
  $time = "00:15:00"
  $rest = other_args[4..-1]
end

$classpath = `#{$bin_dir}/create-class-path #{basedir} #{jar}`.chomp
$port = ENV['CASHMERE_PORT']


