import { ChevronDown, ChevronUp, SlidersHorizontal } from "lucide-react";
import { useId, useState } from "react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group";

import { type Category, NeedWantTypeSchema } from "../../../types/api";
import { countActiveFilters, type TransactionFiltersValue } from "../types";

interface TransactionFiltersProps {
  value: TransactionFiltersValue;
  onChange: (next: TransactionFiltersValue) => void;
  categories: Category[];
}

export function TransactionFilters({ value, onChange, categories }: TransactionFiltersProps) {
  const [isOpen, setIsOpen] = useState(false);
  const keywordId = useId();
  const panelId = useId();
  const activeCount = countActiveFilters(value);

  const handleKeywordChange = (keyword: string) => {
    onChange({ ...value, keyword });
  };

  const handleNeedWantChange = (next: string) => {
    if (next === "") {
      onChange({ ...value, needWantType: null });
      return;
    }
    const parsed = NeedWantTypeSchema.safeParse(next);
    if (!parsed.success) return;
    onChange({ ...value, needWantType: parsed.data });
  };

  const toggleCategory = (categoryId: number) => {
    const exists = value.categoryIds.includes(categoryId);
    const nextIds = exists
      ? value.categoryIds.filter((id) => id !== categoryId)
      : [...value.categoryIds, categoryId];
    onChange({ ...value, categoryIds: nextIds });
  };

  const categoriesLabel =
    value.categoryIds.length === 0
      ? "Categories"
      : `Categories (${String(value.categoryIds.length)})`;

  return (
    <div className="flex flex-col gap-2">
      <Button
        type="button"
        variant="outline"
        size="sm"
        aria-expanded={isOpen}
        aria-controls={panelId}
        onClick={() => {
          setIsOpen((prev) => !prev);
        }}
        className="self-start"
      >
        <SlidersHorizontal />
        <span>Filters</span>
        {activeCount > 0 && (
          <Badge aria-label="Active filter count" variant="secondary">
            {activeCount}
          </Badge>
        )}
        {isOpen ? <ChevronUp /> : <ChevronDown />}
      </Button>
      {isOpen && (
        <div id={panelId} className="border-border flex flex-col gap-3 rounded-md border p-3">
          <div className="flex flex-col gap-1.5">
            <Label htmlFor={keywordId}>Keyword</Label>
            <Input
              id={keywordId}
              type="search"
              value={value.keyword}
              placeholder="Search title or memo"
              onChange={(event) => {
                handleKeywordChange(event.target.value);
              }}
            />
          </div>
          <div className="flex flex-col gap-1.5">
            <span className="text-sm leading-none font-medium">Category</span>
            <Popover>
              <PopoverTrigger asChild>
                <Button type="button" variant="outline" size="sm" className="self-start">
                  {categoriesLabel}
                </Button>
              </PopoverTrigger>
              <PopoverContent align="start">
                <ul className="flex flex-col gap-2">
                  {categories.map((category) => {
                    const checkboxId = `${panelId}-cat-${String(category.id)}`;
                    const checked = value.categoryIds.includes(category.id);
                    return (
                      <li key={category.id} className="flex items-center gap-2">
                        <Checkbox
                          id={checkboxId}
                          checked={checked}
                          onCheckedChange={() => {
                            toggleCategory(category.id);
                          }}
                          aria-label={category.name}
                        />
                        <Label htmlFor={checkboxId} className="font-normal">
                          {category.name}
                        </Label>
                      </li>
                    );
                  })}
                </ul>
              </PopoverContent>
            </Popover>
          </div>
          <div className="flex flex-col gap-1.5">
            <span className="text-sm leading-none font-medium">Need / Want</span>
            <ToggleGroup
              type="single"
              value={value.needWantType ?? ""}
              onValueChange={handleNeedWantChange}
              className="self-start"
            >
              <ToggleGroupItem value="NEED" aria-label="NEED">
                NEED
              </ToggleGroupItem>
              <ToggleGroupItem value="WANT" aria-label="WANT">
                WANT
              </ToggleGroupItem>
              <ToggleGroupItem value="UNSET" aria-label="UNSET">
                UNSET
              </ToggleGroupItem>
            </ToggleGroup>
          </div>
        </div>
      )}
    </div>
  );
}
