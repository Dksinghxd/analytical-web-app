import { UIGenerationPanel } from "@/components/ui-generation-panel"
import { UIExplanationPanel } from "@/components/ui-explanation-panel"
import { GeneratedPreview } from "@/components/generated-preview"

export default function GeneratorPage() {
  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-balance">UI Generator</h1>
        <p className="text-muted-foreground mt-1">
          AI-powered dashboard generation based on your CI/CD patterns and failure trends
        </p>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-1 space-y-6">
          <UIGenerationPanel />
          <UIExplanationPanel />
        </div>

        <div className="lg:col-span-2">
          <GeneratedPreview />
        </div>
      </div>
    </div>
  )
}
