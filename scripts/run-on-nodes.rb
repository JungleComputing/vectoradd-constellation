
def ask
    puts "Continue, wait, stop? [Y/w/n]"
    answer = $stdin.gets.chomp
    case answer
    when "", "Y", "y"
      return "continue"
    when "w", "W"
      return "wait"
    else
	return "stop"
    end
end


