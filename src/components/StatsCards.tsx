interface StatsCardsProps {
  totalItems: number;
  totalCost: number;
  totalValue: number;
  potentialProfit: number;
}

const StatsCards = ({ totalItems, totalCost, totalValue, potentialProfit }: StatsCardsProps) => {
  const formatCurrency = (amount: number) => {
    return `KES ${amount.toLocaleString()}`;
  };

  return (
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <div className="stat-card animate-fade-in" style={{ animationDelay: "0ms" }}>
        <p className="text-sm text-muted-foreground">Total Items</p>
        <p className="text-2xl font-bold text-foreground">{totalItems}</p>
      </div>

      <div className="stat-card animate-fade-in" style={{ animationDelay: "50ms" }}>
        <p className="text-sm text-muted-foreground">Total Cost</p>
        <p className="text-2xl font-bold text-foreground">{formatCurrency(totalCost)}</p>
      </div>

      <div className="stat-card animate-fade-in" style={{ animationDelay: "100ms" }}>
        <p className="text-sm text-muted-foreground">Total Value</p>
        <p className="text-2xl font-bold text-foreground">{formatCurrency(totalValue)}</p>
      </div>

      <div className="stat-card animate-fade-in" style={{ animationDelay: "150ms" }}>
        <p className="text-sm text-muted-foreground">Potential Profit</p>
        <p className="text-2xl font-bold text-success">{formatCurrency(potentialProfit)}</p>
      </div>
    </div>
  );
};

export default StatsCards;
