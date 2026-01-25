import { useState } from "react";
import { Search, Edit2, Check, X } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";

interface SearchBarProps {
  searchQuery: string;
  setSearchQuery: (query: string) => void;
  categories: string[];
  selectedCategory: string;
  setSelectedCategory: (category: string) => void;
  onRenameCategory?: (oldName: string, newName: string) => void;
}

const SearchBar = ({ 
  searchQuery, 
  setSearchQuery, 
  categories, 
  selectedCategory, 
  setSelectedCategory,
  onRenameCategory 
}: SearchBarProps) => {
  const [editingCategory, setEditingCategory] = useState<string | null>(null);
  const [editValue, setEditValue] = useState("");

  const handleStartEdit = (category: string, e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setEditingCategory(category);
    setEditValue(category);
  };

  const handleSaveEdit = () => {
    if (editingCategory && editValue.trim() && onRenameCategory) {
      onRenameCategory(editingCategory, editValue.trim());
    }
    setEditingCategory(null);
    setEditValue("");
  };

  const handleCancelEdit = () => {
    setEditingCategory(null);
    setEditValue("");
  };

  return (
    <div className="flex gap-2 flex-1">
      <div className="relative flex-1">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input
          type="text"
          placeholder="Search items or descriptions..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="pl-10 bg-secondary border-border"
        />
      </div>
      <Popover>
        <PopoverTrigger asChild>
          <Button variant="outline" className="w-[160px] justify-between bg-secondary border-border">
            <span className="truncate">{selectedCategory === "all" ? "All Categories" : selectedCategory}</span>
          </Button>
        </PopoverTrigger>
        <PopoverContent className="w-[200px] p-2 bg-popover border-border" align="end">
          <div className="space-y-1">
            <Button
              variant={selectedCategory === "all" ? "secondary" : "ghost"}
              className="w-full justify-start text-sm"
              onClick={() => setSelectedCategory("all")}
            >
              All Categories
            </Button>
            {categories.map((category) => (
              <div key={category} className="flex items-center gap-1">
                {editingCategory === category ? (
                  <div className="flex items-center gap-1 flex-1">
                    <Input
                      value={editValue}
                      onChange={(e) => setEditValue(e.target.value)}
                      className="h-8 text-sm bg-secondary"
                      autoFocus
                      onKeyDown={(e) => {
                        if (e.key === "Enter") handleSaveEdit();
                        if (e.key === "Escape") handleCancelEdit();
                      }}
                    />
                    <Button size="icon" variant="ghost" className="h-8 w-8 shrink-0" onClick={handleSaveEdit}>
                      <Check size={14} className="text-green-500" />
                    </Button>
                    <Button size="icon" variant="ghost" className="h-8 w-8 shrink-0" onClick={handleCancelEdit}>
                      <X size={14} className="text-destructive" />
                    </Button>
                  </div>
                ) : (
                  <>
                    <Button
                      variant={selectedCategory === category ? "secondary" : "ghost"}
                      className="flex-1 justify-start text-sm"
                      onClick={() => setSelectedCategory(category)}
                    >
                      {category}
                    </Button>
                    {onRenameCategory && (
                      <Button
                        size="icon"
                        variant="ghost"
                        className="h-8 w-8 shrink-0 opacity-50 hover:opacity-100"
                        onClick={(e) => handleStartEdit(category, e)}
                      >
                        <Edit2 size={14} />
                      </Button>
                    )}
                  </>
                )}
              </div>
            ))}
          </div>
        </PopoverContent>
      </Popover>
    </div>
  );
};

export default SearchBar;